package lombok.eclipse.handlers;

import lombok.Cleanup;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseAST.Node;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.mangosdk.spi.ProviderFor;

@ProviderFor(EclipseAnnotationHandler.class)
public class HandleCleanup implements EclipseAnnotationHandler<Cleanup> {
	public boolean handle(AnnotationValues<Cleanup> annotation, Annotation ast, Node annotationNode) {
		String cleanupName = annotation.getInstance().cleanupMethod();
		if ( cleanupName.length() == 0 ) {
			annotationNode.addError("cleanupName cannot be the empty string.");
			return true;
		}
		
		if ( annotationNode.up().getKind() != Kind.LOCAL ) {
			annotationNode.addError("@Cleanup is legal only on local variable declarations.");
			return true;
		}
		
		LocalDeclaration decl = (LocalDeclaration)annotationNode.up().get();
		
		Node ancestor = annotationNode.up().directUp();
		ASTNode blockNode = ancestor.get();
		
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
			annotationNode.addError("LOMBOK BUG: Parent block does not contain any statements.");
			return true;
		}
		
		int start = 0;
		for ( ; start < statements.length ; start++ ) {
			if ( statements[start] == decl ) break;
		}
		
		if ( start == statements.length ) {
			annotationNode.addError("LOMBOK BUG: Can't find this local variable declaration inside its parent.");
			return true;
		}
		
		start++;  //We start with try{} *AFTER* the var declaration.
		
		int end;
		if ( isSwitch ) {
			end = start + 1;
			for ( ; end < statements.length ; end++ ) {
				if ( statements[end] instanceof CaseStatement ) {
					break;
				}
			}
		} else end = statements.length;
		
		//At this point:
		//  start-1 = Local Declaration marked with @Cleanup
		//  start = first instruction that needs to be wrapped into a try block
		//  end = last intruction of the scope -OR- last instruction before the next case label in switch statements.
		//  hence:
		//  [start, end) = statements for the try block.
		
		Statement[] tryBlock = new Statement[end - start];
		System.arraycopy(statements, start, tryBlock, 0, end-start);
		//Remove the stuff we just dumped into the tryBlock, and then leave room for the try node.
		int newStatementsLength = statements.length - (end-start); //Remove room for every statement moved into try block...
		newStatementsLength += 1; //But add room for the TryStatement node itself.
		Statement[] newStatements = new Statement[newStatementsLength];
		System.arraycopy(statements, 0, newStatements, 0, start); //copy all statements before the try block verbatim.
		System.arraycopy(statements, end, newStatements, start+1, statements.length - end); //For switch statements.
		
		TryStatement tryStatement = new TryStatement();
		tryStatement.tryBlock = new Block(0);
		tryStatement.tryBlock.statements = tryBlock;
		newStatements[start] = tryStatement;
		
		Statement[] finallyBlock = new Statement[1];
		MessageSend unsafeClose = new MessageSend();
		unsafeClose.receiver = new SingleNameReference(decl.name, 0);
		unsafeClose.selector = cleanupName.toCharArray();
		finallyBlock[0] = unsafeClose;
		tryStatement.finallyBlock = new Block(0);
		tryStatement.finallyBlock.statements = finallyBlock;
		
		tryStatement.catchArguments = null;
		tryStatement.catchBlocks = null;
		
		if ( blockNode instanceof AbstractMethodDeclaration ) {
			((AbstractMethodDeclaration)blockNode).statements = newStatements;
		} else if ( blockNode instanceof Block ) {
			((Block)blockNode).statements = newStatements;
		} else if ( blockNode instanceof SwitchStatement ) {
			((SwitchStatement)blockNode).statements = newStatements;
		}
		
		ancestor.rebuild();
		
		
		return true;
	}
}
