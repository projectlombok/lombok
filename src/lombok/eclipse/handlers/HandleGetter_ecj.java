package lombok.eclipse.handlers;

import java.lang.reflect.Modifier;

import lombok.eclipse.EclipseAST.Node;
import lombok.transformations.TransformationsUtil;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
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
	public void apply(Annotation annotation, Node node, FieldDeclaration field) {
		TypeReference fieldType = field.type;
		String getterName = TransformationsUtil.toGetterName(
				new String(field.name), nameEquals(fieldType.getTypeName(), "boolean"));
		
		TypeDeclaration parent = (TypeDeclaration) node.up().getEclipseNode();
		if ( parent.methods != null ) for ( AbstractMethodDeclaration method : parent.methods ) {
			if ( method.selector != null && new String(method.selector).equals(getterName) ) return;
		}
		
		MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
		method.modifiers = Modifier.PUBLIC;
		method.returnType = field.type;
		method.annotations = null;
		method.arguments = null;
		method.selector = getterName.toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.scope = parent.scope == null ? null : new MethodScope(parent.scope, method, false);
		method.bits |= ASTNode.Bit24;
		Expression fieldExpression = new SingleNameReference(field.name, (field.declarationSourceStart << 32) | field.declarationSourceEnd);
		Statement returnStatement = new ReturnStatement(fieldExpression, field.sourceStart, field.sourceEnd);
		method.bodyStart = method.declarationSourceStart = method.sourceStart = annotation.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = annotation.sourceEnd;
		method.statements = new Statement[] { returnStatement };
		if ( parent.methods == null ) {
			parent.methods = new AbstractMethodDeclaration[1];
			parent.methods[0] = method;
		} else {
			AbstractMethodDeclaration[] newArray = new AbstractMethodDeclaration[parent.methods.length + 1];
			System.arraycopy(parent.methods, 0, newArray, 0, parent.methods.length);
			newArray[parent.methods.length] = method;
			parent.methods = newArray;
		}
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
