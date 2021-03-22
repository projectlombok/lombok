/*
 * Copyright (C) 2015-2021 The Project Lombok Authors.
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

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

import lombok.ConfigurationKeys;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.core.AST.Kind;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.UtilityClass;
import lombok.spi.Provides;

/**
 * Handles the {@code lombok.experimental.UtilityClass} annotation for eclipse.
 */
@Provides
@HandlerPriority(-4096) //-2^12; to ensure @FieldDefaults picks up on the 'static' we set here.
public class HandleUtilityClass extends EclipseAnnotationHandler<UtilityClass> {
	@Override public void handle(AnnotationValues<UtilityClass> annotation, Annotation ast, EclipseNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.UTILITY_CLASS_FLAG_USAGE, "@UtilityClass");
		
		EclipseNode typeNode = annotationNode.up();
		if (!checkLegality(typeNode, annotationNode)) return;
		changeModifiersAndGenerateConstructor(annotationNode.up(), annotationNode);
	}
	
	private static boolean checkLegality(EclipseNode typeNode, EclipseNode errorNode) {
		if (!isClass(typeNode)) {
			errorNode.addError("@UtilityClass is only supported on a class.");
			return false;
		}
		
		// It might be an inner class. This is okay, but only if it is / can be a static inner class. Thus, all of its parents have to be static inner classes until the top-level.
		EclipseNode typeWalk = typeNode;
		while (true) {
			typeWalk = typeWalk.up();
			switch (typeWalk.getKind()) {
			case TYPE:
				if ((((TypeDeclaration) typeWalk.get()).modifiers & (ClassFileConstants.AccStatic | ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) != 0) continue;
				if (typeWalk.up().getKind() == Kind.COMPILATION_UNIT) return true;
				errorNode.addError("@UtilityClass automatically makes the class static, however, this class cannot be made static.");
				return false;
			case COMPILATION_UNIT:
				return true;
			default:
				errorNode.addError("@UtilityClass cannot be placed on a method local or anonymous inner class, or any class nested in such a class.");
				return false;
			}
		}
	}
	
	private void changeModifiersAndGenerateConstructor(EclipseNode typeNode, EclipseNode annotationNode) {
		TypeDeclaration classDecl = (TypeDeclaration) typeNode.get();
		
		boolean makeConstructor = true;
		
		classDecl.modifiers |= ClassFileConstants.AccFinal;
		
		boolean markStatic = true;
		boolean requiresClInit = false;
		boolean alreadyHasClinit = false;
		
		if (typeNode.up().getKind() == Kind.COMPILATION_UNIT) markStatic = false;
		if (markStatic && typeNode.up().getKind() == Kind.TYPE) {
			TypeDeclaration typeDecl = (TypeDeclaration) typeNode.up().get();
			if ((typeDecl.modifiers & (ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation)) != 0) markStatic = false;
		}
		
		if (markStatic) classDecl.modifiers |= ClassFileConstants.AccStatic;
		
		for (EclipseNode element : typeNode.down()) {
			if (element.getKind() == Kind.FIELD) {
				FieldDeclaration fieldDecl = (FieldDeclaration) element.get();
				if ((fieldDecl.modifiers & ClassFileConstants.AccStatic) == 0) {
					requiresClInit = true;
					fieldDecl.modifiers |= ClassFileConstants.AccStatic;
				}
			} else if (element.getKind() == Kind.METHOD) {
				AbstractMethodDeclaration amd = (AbstractMethodDeclaration) element.get();
				if (amd instanceof ConstructorDeclaration) {
					ConstructorDeclaration constrDecl = (ConstructorDeclaration) element.get();
					if (getGeneratedBy(constrDecl) == null && (constrDecl.bits & ASTNode.IsDefaultConstructor) == 0) {
						element.addError("@UtilityClasses cannot have declared constructors.");
						makeConstructor = false;
						continue;
					}
				} else if (amd instanceof MethodDeclaration) {
					amd.modifiers |= ClassFileConstants.AccStatic;
				} else if (amd instanceof Clinit) {
					alreadyHasClinit = true;
				}
			} else if (element.getKind() == Kind.TYPE) {
				((TypeDeclaration) element.get()).modifiers |= ClassFileConstants.AccStatic;
			}
		}
		
		if (makeConstructor) createPrivateDefaultConstructor(typeNode, annotationNode);
		if (requiresClInit && !alreadyHasClinit) classDecl.addClinit();
	}
	
	private static final char[][] JAVA_LANG_UNSUPPORTED_OPERATION_EXCEPTION = new char[][] {
		TypeConstants.JAVA, TypeConstants.LANG, "UnsupportedOperationException".toCharArray()
	};
	
	private static final char[] UNSUPPORTED_MESSAGE = "This is a utility class and cannot be instantiated".toCharArray();
	
	private void createPrivateDefaultConstructor(EclipseNode typeNode, EclipseNode sourceNode) {
		ASTNode source = sourceNode.get();
		
		TypeDeclaration typeDeclaration = ((TypeDeclaration) typeNode.get());
		
		ConstructorDeclaration constructor = new ConstructorDeclaration(((CompilationUnitDeclaration) typeNode.top().get()).compilationResult);
		
		constructor.modifiers = ClassFileConstants.AccPrivate;
		constructor.selector = typeDeclaration.name;
		constructor.constructorCall = new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);
		constructor.thrownExceptions = null;
		constructor.typeParameters = null;
		constructor.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		constructor.arguments = null;
		
		long[] ps = new long[JAVA_LANG_UNSUPPORTED_OPERATION_EXCEPTION.length];
		AllocationExpression exception = new AllocationExpression();
		exception.type = new QualifiedTypeReference(JAVA_LANG_UNSUPPORTED_OPERATION_EXCEPTION, ps);
		exception.arguments = new Expression[] {
				new StringLiteral(UNSUPPORTED_MESSAGE, 0, 0, 0)
		};
		ThrowStatement throwStatement = new ThrowStatement(exception, 0, 0);
		
		constructor.statements = new Statement[] {throwStatement};
		
		constructor.traverse(new SetGeneratedByVisitor(source), typeDeclaration.scope);
		injectMethod(typeNode, constructor);
	}
}
