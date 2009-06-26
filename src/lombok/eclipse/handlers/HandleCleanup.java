package lombok.eclipse.handlers;

import static lombok.eclipse.Eclipse.copyType;

import java.util.Arrays;

import lombok.Cleanup;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseAST.Node;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.mangosdk.spi.ProviderFor;

@ProviderFor(EclipseAnnotationHandler.class)
public class HandleCleanup implements EclipseAnnotationHandler<Cleanup> {
	@Override public boolean handle(AnnotationValues<Cleanup> annotation, Annotation ast, Node annotationNode) {
		String cleanupName = annotation.getInstance().cleanupMethod();
		if ( cleanupName.isEmpty() ) {
			annotationNode.addError("cleanupName cannot be the empty string.");
			return true;
		}
		
		if ( annotationNode.up().getKind() != Kind.LOCAL ) {
			annotationNode.addError("@Cleanup is legal only on local variable declarations.");
			return true;
		}
		
		LocalDeclaration decl = (LocalDeclaration)annotationNode.up().get();
		
		ASTNode blockNode = annotationNode.up().directUp().get();
		
		final boolean isSwitch;
		final Statement[] statements;
		if ( blockNode instanceof AbstractMethodDeclaration ) {
			isSwitch = false;
			statements = ((AbstractMethodDeclaration)blockNode).statements;
		} else if ( blockNode instanceof Block ) {
			isSwitch = false;
			statements = ((Block)blockNode).statements;
		} else if ( blockNode instanceof SwitchStatement ) {
			isSwitch = true;
			statements = ((SwitchStatement)blockNode).statements;
		} else {
			annotationNode.addError("@Cleanup is legal only on a local variable declaration inside a block.");
			return true;
		}
		
		if ( statements == null ) {
			annotationNode.addError("Parent block does not contain any statements. This is a lombok bug.");
			return true;
		}
		
		int start = 0;
		for ( ; start < statements.length ; start++ ) {
			if ( statements[start] == decl ) break;
		}
		
		if ( start == statements.length ) {
			annotationNode.addError("Can't find this local variable declaration inside its parent. This is a lombok bug.");
			return true;
		}
		
		start++;
		
		int end = start + 1;
		for ( ; end < statements.length ; end++ ) {
			if ( isSwitch && statements[end] instanceof CaseStatement ) {
				annotationNode.addError("The cleanup method must be called before the next case/default statement.");
				return true;
			}
			if ( statements[end] instanceof MessageSend ) {
				MessageSend ms = (MessageSend)statements[end];
				//The method name is the same as the 'cleanupName = ' field of the @Cleanup annotation...
				if ( ms.selector == null || !cleanupName.equals(new String(ms.selector)) ) continue;
				//The call is of the form 'foo.cleanup(anything)', where foo is a simple reference and not an expression...
				if ( !(ms.receiver instanceof SingleNameReference) ) continue;
				//And the reference is the same name as the local variable annotated with @Cleanup...
				if ( !Arrays.equals(((SingleNameReference)ms.receiver).token, decl.name) ) continue;
				//Then we found it!
				if ( ms.arguments != null && ms.arguments.length > 0 ) {
					//As we'll be moving the close() call around, any references to local vars may not be valid in the new scope.
					//Technically we could throw scope markers around the whole shebang and split local var declarations into a separate
					//declaration (in the newly created top scope) and an initialization, but, then there's 'final' and 'definite assignment'
					//rules to worry about. So, let's make this easy on ourselves and allow no arguments, for now.
					annotationNode.addError("The cleanup method cannot have any arguments.");
					return true;
				}
				break;
			}
		}
		
		if ( end == statements.length ) {
			annotationNode.addError("You need to include a " + new String(decl.name) + "." + cleanupName + "() call at the same scope level.");
			return true;
		}
		
		//At this point, at start-1, there's the local declaration, and at end, there's the close call.
		//Thus, we need to move [start, end) into a try block, and move the close call to its own scope.
		
		Statement[] tryBlock = new Statement[end - start];
		System.arraycopy(statements, start, tryBlock, 0, end-start);
		//Remove the stuff we just dumped into the tryBlock, AND the close() call, and then leave room for the try node and the unique name.
		Statement[] newStatements = new Statement[statements.length - (end-start) +1];
		System.arraycopy(statements, 0, newStatements, 0, start);
		if ( statements.length - end > 0 ) System.arraycopy(statements, end+1, newStatements, start+2, statements.length - end -1);
		TryStatement tryStatement = new TryStatement();
		newStatements[start+1] = tryStatement;
		LocalDeclaration tempVar = new LocalDeclaration(("$lombok$cleanup$" + new String(decl.name)).toCharArray(), 0, 0);
		tempVar.type = TypeReference.baseTypeReference(TypeIds.T_boolean, 0);
		tempVar.initialization = new FalseLiteral(0, 0);
		newStatements[start] = tempVar;
		tryStatement.tryBlock = new Block(0);
		tryStatement.tryBlock.statements = tryBlock;
		
		char[] exName = ("$lombok$cleanup$ex$" + new String(decl.name)).toCharArray();
		Statement[] finallyBlock = new Statement[1];
		TryStatement safeClose = new TryStatement();
		safeClose.tryBlock = new Block(0);
		safeClose.tryBlock.statements = new Statement[1];
		MessageSend newCloseCall = new MessageSend();
		newCloseCall.receiver = new SingleNameReference(decl.name, 0);
		newCloseCall.selector = cleanupName.toCharArray();
		safeClose.tryBlock.statements[0] = newCloseCall;
		safeClose.catchArguments = new Argument[1];
		safeClose.catchArguments[0] = new Argument(exName, 0,
				new QualifiedTypeReference(TypeConstants.JAVA_LANG_THROWABLE, new long[] { 0, 0, 0}), 0);
		safeClose.catchBlocks = new Block[1];
		safeClose.catchBlocks[0] = new Block(0);
		safeClose.catchBlocks[0].sourceEnd = safeClose.catchBlocks[0].sourceStart = -2;
		MessageSend unsafeClose = new MessageSend();
		unsafeClose.receiver = new SingleNameReference(decl.name, 0);
		unsafeClose.selector = cleanupName.toCharArray();
		finallyBlock[0] = new IfStatement(new SingleNameReference(tempVar.name, 0), safeClose, unsafeClose, 0, 0);
		tryStatement.finallyBlock = new Block(0);
		tryStatement.finallyBlock.statements = finallyBlock;
		
		Node containingMethodNode = annotationNode;
		TypeReference[] thrownExceptions = null;
		findThrownExceptions:
		while ( containingMethodNode != null ) {
			switch ( containingMethodNode.getKind() ) {
			case INITIALIZER:
				break findThrownExceptions;
			case METHOD:
				thrownExceptions = ((AbstractMethodDeclaration)containingMethodNode.get()).thrownExceptions;
				break findThrownExceptions;
			default:
				containingMethodNode = containingMethodNode.up();
			}
		}
		
		if ( thrownExceptions == null ) thrownExceptions = new TypeReference[0];
		tryStatement.catchArguments = new Argument[thrownExceptions.length + 2];
		tryStatement.catchBlocks = new Block[thrownExceptions.length + 2];
		int idx = 0;
		tryStatement.catchArguments[idx++] = new Argument(exName, 0,
				new QualifiedTypeReference(TypeConstants.JAVA_LANG_RUNTIMEEXCEPTION, new long[] { 0, 0, 0 }), 0);
		tryStatement.catchArguments[idx++] = new Argument(exName, 0,
				new QualifiedTypeReference(TypeConstants.JAVA_LANG_ERROR, new long[] { 0, 0, 0 }), 0);
		for ( ; idx < tryStatement.catchArguments.length ; idx++ ) {
			tryStatement.catchArguments[idx] = new Argument(exName, 0, copyType(thrownExceptions[idx-2]), 0);
		}
		
		for ( idx = 0 ; idx < tryStatement.catchBlocks.length ; idx++ ) {
			Block b = new Block(0);
			tryStatement.catchBlocks[idx] = b;
			b.statements = new Statement[2];
			b.statements[0] = new Assignment(new SingleNameReference(tempVar.name, 0), new TrueLiteral(0, 0), 0);
			b.statements[1] = new ThrowStatement(new SingleNameReference(exName, 0), 0, 0);
			b.sourceEnd = b.sourceStart = -2;
		}
		
		if ( blockNode instanceof AbstractMethodDeclaration ) {
			((AbstractMethodDeclaration)blockNode).statements = newStatements;
		} else if ( blockNode instanceof Block ) {
			((Block)blockNode).statements = newStatements;
		} else if ( blockNode instanceof SwitchStatement ) {
			((SwitchStatement)blockNode).statements = newStatements;
		}
		
		return true;
	}
}
