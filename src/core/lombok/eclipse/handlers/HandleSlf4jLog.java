/*
 * Copyright © 2009 Reinier Zwitserloot, Roel Spilker and Robbert Jan Grootjans.
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

import static lombok.eclipse.Eclipse.fromQualifiedName;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.MemberExistsResult;
import lombok.slf4j.Log;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.mangosdk.spi.ProviderFor;

/**
 * Handles the {@code lombok.HandleSneakyThrows} annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleSlf4jLog implements EclipseAnnotationHandler<Log> {
	
	@Override public boolean handle(AnnotationValues<Log> annotation, Annotation source, EclipseNode annotationNode) {
		
		String loggingClassName = annotation.getRawExpression("value");
		if (loggingClassName == null) loggingClassName = "void.class";
		if (!loggingClassName.endsWith(".class")) loggingClassName = loggingClassName + ".class";
		
		EclipseNode owner = annotationNode.up();
		switch (owner.getKind()) {
		case TYPE:
			TypeDeclaration typeDecl = null;
			if (owner.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) owner.get();
			int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
			
			boolean notAClass = (modifiers &
					(ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation)) != 0;
			
			if (typeDecl == null || notAClass) {
				annotationNode.addError("@Log is legal only on classes and enums.");
				return false;
			}
			
			
			if (loggingClassName.equals("void.class")) {
				loggingClassName = getSelfName(owner);
			}
			
			return handleType(annotationNode, source, loggingClassName);
		default:
			annotationNode.addError("@Log is legal only on types.");
			return true;
		}
	}
	
	private String getSelfName(EclipseNode type) {
		String typeName = getSingleTypeName(type);
		EclipseNode upType = type.up();
		while (upType.getKind() == Kind.TYPE) {
			typeName = getSingleTypeName(upType) + "." + typeName;
			upType = upType.up();
		}
		String packageDeclaration = type.getPackageDeclaration();
		if (packageDeclaration != null) {
			typeName = packageDeclaration + "." + typeName;
		}
		return typeName + ".class";
	}
	
	private String getSingleTypeName(EclipseNode type) {
		TypeDeclaration typeDeclaration = (TypeDeclaration)type.get();
		char[] rawTypeName = typeDeclaration.name;
		return rawTypeName == null ? "" : new String(rawTypeName);
	}
	
	private boolean handleType(EclipseNode annotation, Annotation source, String loggingClassName) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		EclipseNode typeNode = annotation.up();

		MemberExistsResult fieldExists = fieldExists("log", typeNode);
		switch (fieldExists) {
		case NOT_EXISTS:
			// 	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerLog4j.class);

			FieldDeclaration fieldDecl = new FieldDeclaration("log".toCharArray(), 0, -1);
			Eclipse.setGeneratedBy(fieldDecl, source);
			fieldDecl.declarationSourceEnd = -1;
			fieldDecl.modifiers = Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL;
			
			fieldDecl.type = new QualifiedTypeReference(fromQualifiedName("org.slf4j.Logger"), new long[]{p, p, p});
			Eclipse.setGeneratedBy(fieldDecl.type, source);
			
			MessageSend factoryMethodCall = new MessageSend();
			Eclipse.setGeneratedBy(factoryMethodCall, source);
			factoryMethodCall.receiver = new QualifiedNameReference(fromQualifiedName("org.slf4j.LoggerFactory"), new long[] { p, p, p }, pS, pE);
			Eclipse.setGeneratedBy(factoryMethodCall.receiver, source);
			factoryMethodCall.receiver.statementEnd = pE;
			factoryMethodCall.selector = "getLogger".toCharArray();
			
			char[][] loggingClassNameTokens = fromQualifiedName(loggingClassName);
			long[] posses = new long[loggingClassNameTokens.length];
			Arrays.fill(posses, p);
			
			QualifiedNameReference exRef = new QualifiedNameReference(loggingClassNameTokens, posses, pS, pE);
			Eclipse.setGeneratedBy(exRef, source);
			exRef.statementEnd = pE;
			
			factoryMethodCall.arguments = new Expression[] { exRef };
			factoryMethodCall.nameSourcePosition = p;
			factoryMethodCall.sourceStart = pS;
			factoryMethodCall.sourceEnd = factoryMethodCall.statementEnd = pE;
			
			fieldDecl.initialization = factoryMethodCall;
			injectField(annotation.up(), fieldDecl);

			annotation.up().rebuild();
			
			return true;
		case EXISTS_BY_USER:
			annotation.addWarning("Field 'log' already exists.");
		}
		
		return true;
	}
}
