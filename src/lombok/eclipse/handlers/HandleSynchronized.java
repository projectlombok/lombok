package lombok.eclipse.handlers;

import static lombok.eclipse.handlers.PKG.*;

import java.lang.reflect.Modifier;

import lombok.Synchronized;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseAST.Node;
import lombok.eclipse.handlers.PKG.MemberExistsResult;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.mangosdk.spi.ProviderFor;

@ProviderFor(EclipseAnnotationHandler.class)
public class HandleSynchronized implements EclipseAnnotationHandler<Synchronized> {
	private static final char[] INSTANCE_LOCK_NAME = "$lock".toCharArray();
	private static final char[] STATIC_LOCK_NAME = "$LOCK".toCharArray();
	
	@Override public boolean handle(AnnotationValues<Synchronized> annotation, Annotation ast, Node annotationNode) {
		int p1 = ast.sourceStart -1;
		int p2 = ast.sourceStart -2;
		long pos = (((long)p1) << 32) | p2;
		Node methodNode = annotationNode.up();
		if ( methodNode == null || methodNode.getKind() != Kind.METHOD || !(methodNode.get() instanceof MethodDeclaration) ) {
			annotationNode.addError("@Synchronized is legal only on methods.");
			return true;
		}
		
		MethodDeclaration method = (MethodDeclaration)methodNode.get();
		if ( method.isAbstract() ) {
			annotationNode.addError("@Synchronized is legal only on concrete methods.");
			return true;
		}
		
		char[] lockName = annotation.getInstance().value().toCharArray();
		if ( lockName.length == 0 ) lockName = method.isStatic() ? STATIC_LOCK_NAME : INSTANCE_LOCK_NAME;
		
		if ( fieldExists(new String(lockName), methodNode) == MemberExistsResult.NOT_EXISTS ) {
			FieldDeclaration fieldDecl = new FieldDeclaration(lockName, 0, -1);
			fieldDecl.declarationSourceEnd = -1;
			
			fieldDecl.modifiers = (method.isStatic() ? Modifier.STATIC : 0) | Modifier.FINAL | Modifier.PRIVATE;
			
			//We use 'new Object[0];' because quite unlike 'new Object();', empty arrays *ARE* serializable!
			ArrayAllocationExpression arrayAlloc = new ArrayAllocationExpression();
			arrayAlloc.dimensions = new Expression[] { new IntLiteral(new char[] { '0' }, 0, 0) };
			arrayAlloc.type = new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, new long[] { 0, 0, 0 });
			fieldDecl.type = new QualifiedTypeReference(TypeConstants.JAVA_LANG_OBJECT, new long[] { 0, 0, 0 });
			fieldDecl.initialization = arrayAlloc;
			injectField(annotationNode.up().up(), fieldDecl);
		}
		
		if ( method.statements == null ) return false;
		
		Block block = new Block(0);
		block.statements = method.statements;
		Expression lockVariable;
		if ( method.isStatic() ) lockVariable = new QualifiedNameReference(new char[][] {
				methodNode.up().getName().toCharArray(), lockName }, new long[] { pos, pos }, p1, p2);
		else {
			lockVariable = new FieldReference(lockName, pos);
			((FieldReference)lockVariable).receiver = new ThisReference(p1, p2);
		}
		
		method.statements = new Statement[] {
				new SynchronizedStatement(lockVariable, block, 0, 0)
		};
		
		methodNode.rebuild();
		
		return true;
	}
}
