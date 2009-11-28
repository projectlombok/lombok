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
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.core.handlers.TransformationsUtil;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.MemberExistsResult;

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
 * Handles the {@code lombok.Data} annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleData implements EclipseAnnotationHandler<Data> {
	public boolean handle(AnnotationValues<Data> annotation, Annotation ast, EclipseNode annotationNode) {
		Data ann = annotation.getInstance();
		EclipseNode typeNode = annotationNode.up();
		
		TypeDeclaration typeDecl = null;
		if (typeNode.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) typeNode.get();
		int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
		boolean notAClass = (modifiers &
				(ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) != 0;
		
		if (typeDecl == null || notAClass) {
			annotationNode.addError("@Data is only supported on a class.");
			return false;
		}
		
		List<EclipseNode> nodesForConstructor = new ArrayList<EclipseNode>();
		for (EclipseNode child : typeNode.down()) {
			if (child.getKind() != Kind.FIELD) continue;
			FieldDeclaration fieldDecl = (FieldDeclaration) child.get();
			//Skip fields that start with $
			if (fieldDecl.name.length > 0 && fieldDecl.name[0] == '$') continue;
			//Skip static fields.
			if ((fieldDecl.modifiers & ClassFileConstants.AccStatic) != 0) continue;
			boolean isFinal = (fieldDecl.modifiers & ClassFileConstants.AccFinal) != 0;
			boolean isNonNull = findAnnotations(fieldDecl, TransformationsUtil.NON_NULL_PATTERN).length != 0;
			if ((isFinal || isNonNull) && fieldDecl.initialization == null) nodesForConstructor.add(child);
			new HandleGetter().generateGetterForField(child, annotationNode.get());
			if (!isFinal) new HandleSetter().generateSetterForField(child, annotationNode.get());
		}
		
		new HandleToString().generateToStringForType(typeNode, annotationNode);
		new HandleEqualsAndHashCode().generateEqualsAndHashCodeForType(typeNode, annotationNode);
		
		//Careful: Generate the public static constructor (if there is one) LAST, so that any attempt to
		//'find callers' on the annotation node will find callers of the constructor, which is by far the
		//most useful of the many methods built by @Data. This trick won't work for the non-static constructor,
		//for whatever reason, though you can find callers of that one by focusing on the class name itself
		//and hitting 'find callers'.
		
		if (constructorExists(typeNode) == MemberExistsResult.NOT_EXISTS) {
			ConstructorDeclaration constructor = createConstructor(
					ann.staticConstructor().length() == 0, typeNode, nodesForConstructor, ast);
			injectMethod(typeNode, constructor);
		}
		
		if (ann.staticConstructor().length() > 0) {
			if (methodExists("of", typeNode) == MemberExistsResult.NOT_EXISTS) {
				MethodDeclaration staticConstructor = createStaticConstructor(
						ann.staticConstructor(), typeNode, nodesForConstructor, ast);
				injectMethod(typeNode, staticConstructor);
			}
		}
		
		return false;
	}
	
	private ConstructorDeclaration createConstructor(boolean isPublic,
			EclipseNode type, Collection<EclipseNode> fields, ASTNode source) {
		long p = (long)source.sourceStart << 32 | source.sourceEnd;
		
		ConstructorDeclaration constructor = new ConstructorDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		Eclipse.setGeneratedBy(constructor, source);
		
		constructor.modifiers = EclipseHandlerUtil.toEclipseModifier(isPublic ? AccessLevel.PUBLIC : AccessLevel.PRIVATE);
		constructor.annotations = null;
		constructor.selector = ((TypeDeclaration)type.get()).name;
		constructor.constructorCall = new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);
		Eclipse.setGeneratedBy(constructor.constructorCall, source);
		constructor.thrownExceptions = null;
		constructor.typeParameters = null;
		constructor.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		constructor.bodyStart = constructor.declarationSourceStart = constructor.sourceStart = source.sourceStart;
		constructor.bodyEnd = constructor.declarationSourceEnd = constructor.sourceEnd = source.sourceEnd;
		constructor.arguments = null;
		
		List<Argument> args = new ArrayList<Argument>();
		List<Statement> assigns = new ArrayList<Statement>();
		List<Statement> nullChecks = new ArrayList<Statement>();
		
		for (EclipseNode fieldNode : fields) {
			FieldDeclaration field = (FieldDeclaration) fieldNode.get();
			FieldReference thisX = new FieldReference(("this." + new String(field.name)).toCharArray(), p);
			Eclipse.setGeneratedBy(thisX, source);
			thisX.receiver = new ThisReference((int)(p >> 32), (int)p);
			Eclipse.setGeneratedBy(thisX.receiver, source);
			thisX.token = field.name;
			
			SingleNameReference assignmentNameRef = new SingleNameReference(field.name, p);
			Eclipse.setGeneratedBy(assignmentNameRef, source);
			Assignment assignment = new Assignment(thisX, assignmentNameRef, (int)p);
			Eclipse.setGeneratedBy(assignment, source);
			assigns.add(assignment);
			long fieldPos = (((long)field.sourceStart) << 32) | field.sourceEnd;
			Argument argument = new Argument(field.name, fieldPos, copyType(field.type, source), Modifier.FINAL);
			Eclipse.setGeneratedBy(argument, source);
			Annotation[] nonNulls = findAnnotations(field, TransformationsUtil.NON_NULL_PATTERN);
			Annotation[] nullables = findAnnotations(field, TransformationsUtil.NULLABLE_PATTERN);
			if (nonNulls.length != 0) {
				Statement nullCheck = generateNullCheck(field, source);
				if (nullCheck != null) nullChecks.add(nullCheck);
			}
			Annotation[] copiedAnnotations = copyAnnotations(nonNulls, nullables, source);
			if (copiedAnnotations.length != 0) argument.annotations = copiedAnnotations;
			args.add(argument);
		}
		
		nullChecks.addAll(assigns);
		constructor.statements = nullChecks.isEmpty() ? null : nullChecks.toArray(new Statement[nullChecks.size()]);
		constructor.arguments = args.isEmpty() ? null : args.toArray(new Argument[args.size()]);
		return constructor;
	}
	
	private MethodDeclaration createStaticConstructor(String name, EclipseNode type, Collection<EclipseNode> fields, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		MethodDeclaration constructor = new MethodDeclaration(
				((CompilationUnitDeclaration) type.top().get()).compilationResult);
		Eclipse.setGeneratedBy(constructor, source);
		
		constructor.modifiers = EclipseHandlerUtil.toEclipseModifier(AccessLevel.PUBLIC) | Modifier.STATIC;
		TypeDeclaration typeDecl = (TypeDeclaration) type.get();
		if (typeDecl.typeParameters != null && typeDecl.typeParameters.length > 0) {
			TypeReference[] refs = new TypeReference[typeDecl.typeParameters.length];
			int idx = 0;
			for (TypeParameter param : typeDecl.typeParameters) {
				TypeReference typeRef = new SingleTypeReference(param.name, (long)param.sourceStart << 32 | param.sourceEnd);
				Eclipse.setGeneratedBy(typeRef, source);
				refs[idx++] = typeRef;
			}
			constructor.returnType = new ParameterizedSingleTypeReference(typeDecl.name, refs, 0, p);
		} else constructor.returnType = new SingleTypeReference(((TypeDeclaration)type.get()).name, p);
		Eclipse.setGeneratedBy(constructor.returnType, source);
		constructor.annotations = null;
		constructor.selector = name.toCharArray();
		constructor.thrownExceptions = null;
		constructor.typeParameters = copyTypeParams(((TypeDeclaration)type.get()).typeParameters, source);
		constructor.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		constructor.bodyStart = constructor.declarationSourceStart = constructor.sourceStart = source.sourceStart;
		constructor.bodyEnd = constructor.declarationSourceEnd = constructor.sourceEnd = source.sourceEnd;
		
		List<Argument> args = new ArrayList<Argument>();
		List<Expression> assigns = new ArrayList<Expression>();
		AllocationExpression statement = new AllocationExpression();
		statement.sourceStart = pS; statement.sourceEnd = pE;
		Eclipse.setGeneratedBy(statement, source);
		statement.type = copyType(constructor.returnType, source);
		
		for (EclipseNode fieldNode : fields) {
			FieldDeclaration field = (FieldDeclaration) fieldNode.get();
			long fieldPos = (((long)field.sourceStart) << 32) | field.sourceEnd;
			SingleNameReference nameRef = new SingleNameReference(field.name, fieldPos);
			Eclipse.setGeneratedBy(nameRef, source);
			assigns.add(nameRef);
			
			Argument argument = new Argument(field.name, fieldPos, copyType(field.type, source), 0);
			Eclipse.setGeneratedBy(argument, source);

			Annotation[] copiedAnnotations = copyAnnotations(
					findAnnotations(field, TransformationsUtil.NON_NULL_PATTERN),
					findAnnotations(field, TransformationsUtil.NULLABLE_PATTERN), source);
			if (copiedAnnotations.length != 0) argument.annotations = copiedAnnotations;
			args.add(new Argument(field.name, fieldPos, copyType(field.type, source), Modifier.FINAL));
		}
		
		statement.arguments = assigns.isEmpty() ? null : assigns.toArray(new Expression[assigns.size()]);
		constructor.arguments = args.isEmpty() ? null : args.toArray(new Argument[args.size()]);
		constructor.statements = new Statement[] { new ReturnStatement(statement, (int)(p >> 32), (int)p) };
		Eclipse.setGeneratedBy(constructor.statements[0], source);
		return constructor;
	}
}
