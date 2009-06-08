package lombok.agent.eclipse;

import java.lang.reflect.Modifier;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;

public class TransformCompilationUnitDeclaration {
	/** This is a 'magic' method signature - it is this one that will be called. Don't rename anything! */
	
	public static void transform(CompilationUnitDeclaration ast) {
		if ( ast.types != null ) for ( TypeDeclaration type : ast.types ) {
			if ( type.fields != null ) for ( FieldDeclaration field : type.fields ) {
				if ( field.annotations != null ) for ( Annotation annotation : field.annotations ) {
					if ( annotation.type.toString().equals("Getter") ) addGetter(type);
				}
			}
		}
	}
	
	private static void addGetter(TypeDeclaration type) {
		for ( AbstractMethodDeclaration method : type.methods ) {
			if ( method.selector != null && new String(method.selector).equals("getFoo") ) return;
		}
		
		MethodDeclaration method = new MethodDeclaration(type.compilationResult);
		method.modifiers = Modifier.PUBLIC;
		method.returnType = TypeReference.baseTypeReference(TypeReference.T_int, 0);
		method.annotations = null;
		method.arguments = null;
		method.selector = "getFoo".toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.scope = new MethodScope(type.scope, method, false);
		Expression fieldExpression = new SingleNameReference("foo".toCharArray(), 10);
		Statement returnStatement = new ReturnStatement(fieldExpression, 1, 2);
		method.statements = new Statement[] { returnStatement };
		AbstractMethodDeclaration[] newArray = new AbstractMethodDeclaration[type.methods.length + 1];
		System.arraycopy(type.methods, 0, newArray, 0, type.methods.length);
		newArray[type.methods.length] = method;
		type.methods = newArray;
		System.out.println("Generated getFoo method");
	}
}
