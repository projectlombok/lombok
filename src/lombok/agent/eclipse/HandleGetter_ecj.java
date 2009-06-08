package lombok.agent.eclipse;

import java.lang.reflect.Modifier;

import lombok.transformations.TransformationsUtil;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;

public class HandleGetter_ecj {
	public void apply(TypeDeclaration type, FieldDeclaration field) {
		TypeReference fieldType = field.type;
		String getterName = TransformationsUtil.toGetterName(new String(field.name), nameEquals(fieldType.getTypeName(), "boolean"));
		
		for ( AbstractMethodDeclaration method : type.methods ) {
			if ( method.selector != null && new String(method.selector).equals(getterName) ) return;
		}
		
		MethodDeclaration method = new MethodDeclaration(type.compilationResult);
		method.modifiers = Modifier.PUBLIC;
		method.returnType = field.type;
		method.annotations = null;
		method.arguments = null;
		method.selector = getterName.toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.scope = new MethodScope(type.scope, method, false);
		Expression fieldExpression = new SingleNameReference(field.name, (field.declarationSourceStart << 32) | field.declarationSourceEnd);
		Statement returnStatement = new ReturnStatement(fieldExpression, field.sourceStart, field.sourceEnd);
		method.statements = new Statement[] { returnStatement };
		AbstractMethodDeclaration[] newArray = new AbstractMethodDeclaration[type.methods.length + 1];
		System.arraycopy(type.methods, 0, newArray, 0, type.methods.length);
		newArray[type.methods.length] = method;
		type.methods = newArray;
	}
	
	private boolean nameEquals(char[][] typeName, String string) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for ( char[] elem : typeName ) {
			if ( first ) first = false;
			else sb.append('.');
			sb.append(elem);
		}
		
		return string.contentEquals(sb);
	}
}
