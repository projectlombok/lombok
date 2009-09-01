/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.eclipse.handlers;

import static lombok.eclipse.handlers.PKG.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.ToString;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseAST.Node;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.mangosdk.spi.ProviderFor;

/**
 * Handles the <code>ToString</code> annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleToString implements EclipseAnnotationHandler<ToString> {
	private void checkForBogusExcludes(Node type, AnnotationValues<ToString> annotation) {
		List<String> list = Arrays.asList(annotation.getInstance().exclude());
		boolean[] matched = new boolean[list.size()];
		
		for ( Node child : type.down() ) {
			if ( list.isEmpty() ) break;
			if ( child.getKind() != Kind.FIELD ) continue;
			if ( (((FieldDeclaration)child.get()).modifiers & ClassFileConstants.AccStatic) != 0 ) continue;
			int idx = list.indexOf(child.getName());
			if ( idx > -1 ) matched[idx] = true;
		}
		
		for ( int i = 0 ; i < list.size() ; i++ ) {
			if ( !matched[i] ) {
				annotation.setWarning("exclude", "This field does not exist, or would have been excluded anyway.", i);
			}
		}
	}
	
	public void generateToStringForType(Node typeNode, Node errorNode) {
		for ( Node child : typeNode.down() ) {
			if ( child.getKind() == Kind.ANNOTATION ) {
				if ( Eclipse.annotationTypeMatches(ToString.class, child) ) {
					//The annotation will make it happen, so we can skip it.
					return;
				}
			}
		}
		
		boolean includeFieldNames = false;
		boolean callSuper = false;
		try {
			includeFieldNames = ((Boolean)ToString.class.getMethod("includeFieldNames").getDefaultValue()).booleanValue();
		} catch ( Exception ignore ) {}
		try {
			callSuper = ((Boolean)ToString.class.getMethod("callSuper").getDefaultValue()).booleanValue();
		} catch ( Exception ignore ) {}
		generateToString(typeNode, errorNode, Collections.<String>emptyList(), includeFieldNames, callSuper, false);
	}
	
	public boolean handle(AnnotationValues<ToString> annotation, Annotation ast, Node annotationNode) {
		ToString ann = annotation.getInstance();
		List<String> excludes = Arrays.asList(ann.exclude());
		Node typeNode = annotationNode.up();
		
		checkForBogusExcludes(typeNode, annotation);
		
		return generateToString(typeNode, annotationNode, excludes, ann.includeFieldNames(), ann.callSuper(), true);
	}
	
	public boolean generateToString(Node typeNode, Node errorNode, List<String> excludes,
			boolean includeFieldNames, boolean callSuper, boolean whineIfExists) {
		TypeDeclaration typeDecl = null;
		
		if ( typeNode.get() instanceof TypeDeclaration ) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers &
				(ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) != 0;
		
		if ( typeDecl == null || notAClass ) {
			errorNode.addError("@ToString is only supported on a class.");
			return false;
		}
		
		List<Node> nodesForToString = new ArrayList<Node>();
		for ( Node child : typeNode.down() ) {
			if ( child.getKind() != Kind.FIELD ) continue;
			FieldDeclaration fieldDecl = (FieldDeclaration) child.get();
			//Skip static fields.
			if ( (fieldDecl.modifiers & ClassFileConstants.AccStatic) != 0 ) continue;
			//Skip excluded fields.
			if ( excludes.contains(new String(fieldDecl.name)) ) continue;
			nodesForToString.add(child);
		}
		
		switch ( methodExists("toString", typeNode) ) {
		case NOT_EXISTS:
			MethodDeclaration toString = createToString(typeNode, nodesForToString, includeFieldNames, callSuper, errorNode.get());
			injectMethod(typeNode, toString);
			return true;
		case EXISTS_BY_LOMBOK:
			return true;
		default:
		case EXISTS_BY_USER:
			if ( whineIfExists ) {
				errorNode.addWarning("Not generating toString(): A method with that name already exists");
			}
			return true;
		}
	}
	
	private MethodDeclaration createToString(Node type, Collection<Node> fields, boolean includeFieldNames, boolean callSuper, ASTNode pos) {
		TypeDeclaration typeDeclaration = (TypeDeclaration)type.get();
		char[] rawTypeName = typeDeclaration.name;
		String typeName = rawTypeName == null ? "" : new String(rawTypeName);
		char[] suffix = ")".toCharArray();
		String infixS = ", ";
		char[] infix = infixS.toCharArray();
		long p = (long)pos.sourceStart << 32 | pos.sourceEnd;
		final int PLUS = OperatorIds.PLUS;
		
		char[] prefix;
		
		if ( callSuper ) {
			prefix = (typeName + "(super=").toCharArray();
		} else if ( fields.isEmpty() ) {
			prefix = (typeName + "()").toCharArray();
		} else if ( includeFieldNames ) {
			prefix = (typeName + "(" + new String(((FieldDeclaration)fields.iterator().next().get()).name) + "=").toCharArray();
		} else {
			prefix = (typeName + "(").toCharArray();
		}
		
		boolean first = true;
		Expression current = new StringLiteral(prefix, 0, 0, 0);
		if ( callSuper ) {
			MessageSend callToSuper = new MessageSend();
			callToSuper.receiver = new SuperReference(0, 0);
			callToSuper.selector = "toString".toCharArray();
			current = new BinaryExpression(current, callToSuper, PLUS);
			first = false;
		}
		
		for ( Node field : fields ) {
			FieldDeclaration f = (FieldDeclaration)field.get();
			if ( f.name == null || f.type == null ) continue;
			
			Expression ex;
			if ( f.type.dimensions() > 0 ) {
				MessageSend arrayToString = new MessageSend();
				arrayToString.receiver = generateQualifiedNameRef(TypeConstants.JAVA, TypeConstants.UTIL, "Arrays".toCharArray());
				arrayToString.arguments = new Expression[] { new SingleNameReference(f.name, p) };
				if ( f.type.dimensions() > 1 || !BUILT_IN_TYPES.contains(new String(f.type.getLastToken())) ) {
					arrayToString.selector = "deepToString".toCharArray();
				} else {
					arrayToString.selector = "toString".toCharArray();
				}
				ex = arrayToString;
			} else ex = new SingleNameReference(f.name, p);
			
			if ( first ) {
				current = new BinaryExpression(current, ex, PLUS);
				first = false;
				continue;
			}
			
			if ( includeFieldNames ) {
				char[] namePlusEqualsSign = (infixS + new String(f.name) + "=").toCharArray();
				current = new BinaryExpression(current, new StringLiteral(namePlusEqualsSign, 0, 0, 0), PLUS);
			} else {
				current = new BinaryExpression(current, new StringLiteral(infix, 0, 0, 0), PLUS);
			}
			current = new BinaryExpression(current, ex, PLUS);
		}
		if ( !first ) current = new BinaryExpression(current, new StringLiteral(suffix, 0, 0, 0), PLUS);
		
		ReturnStatement returnStatement = new ReturnStatement(current, (int)(p >> 32), (int)p);
		
		MethodDeclaration method = new MethodDeclaration(((CompilationUnitDeclaration) type.top().get()).compilationResult);
		method.modifiers = PKG.toModifier(AccessLevel.PUBLIC);
		method.returnType = new QualifiedTypeReference(TypeConstants.JAVA_LANG_STRING, new long[] {0, 0, 0});
		MarkerAnnotation overrideAnnotation = new MarkerAnnotation(new QualifiedTypeReference(TypeConstants.JAVA_LANG_OVERRIDE, new long[] {p, p, p}), (int)(p >> 32));
		overrideAnnotation.declarationSourceEnd = overrideAnnotation.sourceEnd = overrideAnnotation.statementEnd = (int)p;
		overrideAnnotation.bits |= ASTNode.HasBeenGenerated;
		method.annotations = new Annotation[] {overrideAnnotation};
		method.arguments = null;
		method.selector = "toString".toCharArray();
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = pos.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = pos.sourceEnd;
		method.statements = new Statement[] { returnStatement };
		return method;
	}
	
	private static final Set<String> BUILT_IN_TYPES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"byte", "short", "int", "long", "char", "boolean", "double", "float")));
	
	private NameReference generateQualifiedNameRef(char[]... varNames) {
		if ( varNames.length > 1 )
			return new QualifiedNameReference(varNames, new long[varNames.length], 0, 0);
		else return new SingleNameReference(varNames[0], 0);
	}
}
