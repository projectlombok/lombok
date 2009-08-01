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

import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.PKG.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseAST.Node;
import lombok.eclipse.handlers.PKG.MemberExistsResult;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.mangosdk.spi.ProviderFor;

/**
 * Handles the <code>lombok.Data</code> annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleData implements EclipseAnnotationHandler<Data> {
	public boolean handle(AnnotationValues<Data> annotation, Annotation ast, Node annotationNode) {
		Data ann = annotation.getInstance();
		Node typeNode = annotationNode.up();
		
		TypeDeclaration typeDecl = null;
		if ( typeNode.get() instanceof TypeDeclaration ) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers &
				(ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) != 0;
		
		if ( typeDecl == null || notAClass ) {
			annotationNode.addError("@Data is only supported on a class.");
			return false;
		}
		
		List<Node> nodesForConstructor = new ArrayList<Node>();
		for ( Node child : typeNode.down() ) {
			if ( child.getKind() != Kind.FIELD ) continue;
			FieldDeclaration fieldDecl = (FieldDeclaration) child.get();
			//Skip static fields.
			if ( (fieldDecl.modifiers & ClassFileConstants.AccStatic) != 0 ) continue;
			boolean isFinal = (fieldDecl.modifiers & ClassFileConstants.AccFinal) != 0;
			boolean isNonNull = findNonNullAnnotations(fieldDecl).length != 0;
			if ( (isFinal || isNonNull) && fieldDecl.initialization == null ) nodesForConstructor.add(child);
			new HandleGetter().generateGetterForField(child, annotationNode.get());
			if ( !isFinal ) new HandleSetter().generateSetterForField(child, annotationNode.get());
		}
		
		new HandleToString().generateToStringForType(typeNode, annotationNode);
		new HandleEqualsAndHashCode().generateEqualsAndHashCodeForType(typeNode, annotationNode);
		
		//Careful: Generate the public static constructor (if there is one) LAST, so that any attempt to
		//'find callers' on the annotation node will find callers of the constructor, which is by far the
		//most useful of the many methods built by @Data. This trick won't work for the non-static constructor,
		//for whatever reason, though you can find callers of that one by focussing on the class name itself
		//and hitting 'find callers'.
		
		if ( constructorExists(typeNode) == MemberExistsResult.NOT_EXISTS ) {
			ConstructorDeclaration constructor = createConstructor(
					ann.staticConstructor().length() == 0, typeNode, nodesForConstructor, ast);
			injectMethod(typeNode, constructor);
		}
		
		if ( ann.staticConstructor().length() > 0 ) {
			if ( methodExists("of", typeNode) == MemberExistsResult.NOT_EXISTS ) {
				MethodDeclaration staticConstructor = createStaticConstructor(
						ann.staticConstructor(), typeNode, nodesForConstructor, ast);
				injectMethod(typeNode, staticConstructor);
			}
		}
		
		return false;
	}
	
	private ConstructorDeclaration createConstructor(boolean isPublic, Node type, Collection<Node> fields, ASTNode pos) {
		long p = (long)pos.sourceStart << 32 | pos.sourceEnd;
		
		ConstructorDeclaration constructor = new ConstructorDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		
		constructor.modifiers = PKG.toModifier(isPublic ? AccessLevel.PUBLIC : AccessLevel.PRIVATE);
		constructor.annotations = null;
		constructor.selector = ((TypeDeclaration)type.get()).name;
		constructor.constructorCall = new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);
		constructor.thrownExceptions = null;
		constructor.typeParameters = null;
		constructor.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		constructor.bodyStart = constructor.declarationSourceStart = constructor.sourceStart = pos.sourceStart;
		constructor.bodyEnd = constructor.declarationSourceEnd = constructor.sourceEnd = pos.sourceEnd;
		constructor.arguments = null;
		
		List<Argument> args = new ArrayList<Argument>();
		List<Statement> assigns = new ArrayList<Statement>();
		List<Statement> nullChecks = new ArrayList<Statement>();
		
		for ( Node fieldNode : fields ) {
			FieldDeclaration field = (FieldDeclaration) fieldNode.get();
			FieldReference thisX = new FieldReference(("this." + new String(field.name)).toCharArray(), p);
			thisX.receiver = new ThisReference((int)(p >> 32), (int)p);
			thisX.token = field.name;
			
			assigns.add(new Assignment(thisX, new SingleNameReference(field.name, p), (int)p));
			long fieldPos = (((long)field.sourceStart) << 32) | field.sourceEnd;
			Argument argument = new Argument(field.name, fieldPos, copyType(field.type), 0);
			Annotation[] nonNulls = findNonNullAnnotations(field);
			if (nonNulls.length != 0) {
				nullChecks.add(generateNullCheck(field));
				argument.annotations = copyAnnotations(nonNulls);
			}
			args.add(argument);
		}
		
		nullChecks.addAll(assigns);
		constructor.statements = nullChecks.isEmpty() ? null : nullChecks.toArray(new Statement[nullChecks.size()]);
		constructor.arguments = args.isEmpty() ? null : args.toArray(new Argument[args.size()]);
		return constructor;
	}
	
	private MethodDeclaration createStaticConstructor(String name, Node type, Collection<Node> fields, ASTNode pos) {
		long p = (long)pos.sourceStart << 32 | pos.sourceEnd;
		
		MethodDeclaration constructor = new MethodDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		
		constructor.modifiers = PKG.toModifier(AccessLevel.PUBLIC) | Modifier.STATIC;
		TypeDeclaration typeDecl = (TypeDeclaration) type.get();
		if ( typeDecl.typeParameters != null && typeDecl.typeParameters.length > 0 ) {
			TypeReference[] refs = new TypeReference[typeDecl.typeParameters.length];
			int idx = 0;
			for ( TypeParameter param : typeDecl.typeParameters ) {
				refs[idx++] = new SingleTypeReference(param.name, (long)param.sourceStart << 32 | param.sourceEnd);
			}
			constructor.returnType = new ParameterizedSingleTypeReference(typeDecl.name, refs, 0, p);
		} else constructor.returnType = new SingleTypeReference(((TypeDeclaration)type.get()).name, p);
		constructor.annotations = null;
		constructor.selector = name.toCharArray();
		constructor.thrownExceptions = null;
		constructor.typeParameters = copyTypeParams(((TypeDeclaration)type.get()).typeParameters);
		constructor.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		constructor.bodyStart = constructor.declarationSourceStart = constructor.sourceStart = pos.sourceStart;
		constructor.bodyEnd = constructor.declarationSourceEnd = constructor.sourceEnd = pos.sourceEnd;
		
		List<Argument> args = new ArrayList<Argument>();
		List<Expression> assigns = new ArrayList<Expression>();
		AllocationExpression statement = new AllocationExpression();
		statement.type = copyType(constructor.returnType);
		
		for ( Node fieldNode : fields ) {
			FieldDeclaration field = (FieldDeclaration) fieldNode.get();
			long fieldPos = (((long)field.sourceStart) << 32) | field.sourceEnd;
			assigns.add(new SingleNameReference(field.name, fieldPos));
			
			Argument argument = new Argument(field.name, fieldPos, copyType(field.type), 0);
			Annotation[] nonNulls = findNonNullAnnotations(field);
			if (nonNulls.length != 0) {
				argument.annotations = copyAnnotations(nonNulls);
			}
			args.add(new Argument(field.name, fieldPos, copyType(field.type), 0));
		}
		
		statement.arguments = assigns.isEmpty() ? null : assigns.toArray(new Expression[assigns.size()]);
		constructor.arguments = args.isEmpty() ? null : args.toArray(new Argument[args.size()]);
		constructor.statements = new Statement[] { new ReturnStatement(statement, (int)(p >> 32), (int)p) };
		return constructor;
	}
}
