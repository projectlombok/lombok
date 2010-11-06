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

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.mangosdk.spi.ProviderFor;

/**
 * Handles the {@code lombok.HandleSneakyThrows} annotation for eclipse.
 */
public class HandleLog {
	
	public static boolean processAnnotation(LoggingFramework framework, AnnotationValues<? extends java.lang.annotation.Annotation> annotation, Annotation source, EclipseNode annotationNode) {
		
		String loggingClassName = annotation.getRawExpression("value");
		if (loggingClassName == null) loggingClassName = "void";
		if (loggingClassName.endsWith(".class")) loggingClassName = loggingClassName.substring(0, loggingClassName.length() - 6);
		
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
			
			MemberExistsResult fieldExists = fieldExists("log", owner);
			if (fieldExists != MemberExistsResult.NOT_EXISTS) {
				annotationNode.addWarning("Field 'log' already exists.");
				return true;
			}
			
			if (loggingClassName.equals("void")) {
				loggingClassName = getSelfName(owner);
			}
			
			injectField(owner, createField(framework, source, loggingClassName));
			owner.rebuild();
			return true;
		default:
			annotationNode.addError("@Log is legal only on types.");
			return true;
		}
	}
	
	private static String getSelfName(EclipseNode type) {
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
		return typeName;
	}
	
	private static String getSingleTypeName(EclipseNode type) {
		TypeDeclaration typeDeclaration = (TypeDeclaration)type.get();
		char[] rawTypeName = typeDeclaration.name;
		return rawTypeName == null ? "" : new String(rawTypeName);
	}
	
	private static FieldDeclaration createField(LoggingFramework framework, Annotation source, String loggingClassName) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		// 	private static final <loggerType> log = <factoryMethod>(<parameter>);

		FieldDeclaration fieldDecl = new FieldDeclaration("log".toCharArray(), 0, -1);
		Eclipse.setGeneratedBy(fieldDecl, source);
		fieldDecl.declarationSourceEnd = -1;
		fieldDecl.modifiers = Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL;
		
		fieldDecl.type = createTypeReference(framework.getLoggerTypeName(), source);
		
		MessageSend factoryMethodCall = new MessageSend();
		Eclipse.setGeneratedBy(factoryMethodCall, source);

		factoryMethodCall.receiver = createNameReference(framework.getLoggerFactoryTypeName(), source);
		factoryMethodCall.selector = framework.getLoggerFactoryMethodName().toCharArray();
		
		Expression parameter = framework.createFactoryParameter(loggingClassName, source);
		
		factoryMethodCall.arguments = new Expression[] { parameter };
		factoryMethodCall.nameSourcePosition = p;
		factoryMethodCall.sourceStart = pS;
		factoryMethodCall.sourceEnd = factoryMethodCall.statementEnd = pE;
		
		fieldDecl.initialization = factoryMethodCall;
		
		return fieldDecl;
	}
	
	private static TypeReference createTypeReference(String typeName, Annotation source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		char[][] typeNameTokens = fromQualifiedName(typeName);
		long[] pos = new long[typeNameTokens.length];
		Arrays.fill(pos, p);
		
		QualifiedTypeReference typeReference = new QualifiedTypeReference(typeNameTokens, pos);
		Eclipse.setGeneratedBy(typeReference, source);
		return typeReference;
	}
	
	private static NameReference createNameReference(String name, Annotation source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		char[][] nameTokens = fromQualifiedName(name);
		long[] pos = new long[nameTokens.length];
		Arrays.fill(pos, p);
		
		QualifiedNameReference nameReference = new QualifiedNameReference(nameTokens, pos, pS, pE);
		nameReference.statementEnd = pE;

		Eclipse.setGeneratedBy(nameReference, source);
		return nameReference;
	}
	
	/**
	 * Handles the {@link lombok.commons.Log} annotation for Eclipse.
	 */
	@ProviderFor(EclipseAnnotationHandler.class)
	public static class HandleCommonsLog implements EclipseAnnotationHandler<lombok.commons.Log> {
		@Override public boolean handle(AnnotationValues<lombok.commons.Log> annotation, Annotation source, EclipseNode annotationNode) {
			return processAnnotation(LoggingFramework.COMMONS, annotation, source, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.jul.Log} annotation for Eclipse.
	 */
	@ProviderFor(EclipseAnnotationHandler.class)
	public static class HandleJulLog implements EclipseAnnotationHandler<lombok.jul.Log> {
		@Override public boolean handle(AnnotationValues<lombok.jul.Log> annotation, Annotation source, EclipseNode annotationNode) {
			return processAnnotation(LoggingFramework.JUL, annotation, source, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.log4j.Log} annotation for Eclipse.
	 */
	@ProviderFor(EclipseAnnotationHandler.class)
	public static class HandleLog4jLog implements EclipseAnnotationHandler<lombok.log4j.Log> {
		@Override public boolean handle(AnnotationValues<lombok.log4j.Log> annotation, Annotation source, EclipseNode annotationNode) {
			return processAnnotation(LoggingFramework.LOG4J, annotation, source, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.slf4j.Log} annotation for Eclipse.
	 */
	@ProviderFor(EclipseAnnotationHandler.class)
	public static class HandleSlf4jLog implements EclipseAnnotationHandler<lombok.slf4j.Log> {
		@Override public boolean handle(AnnotationValues<lombok.slf4j.Log> annotation, Annotation source, EclipseNode annotationNode) {
			return processAnnotation(LoggingFramework.SLF4J, annotation, source, annotationNode);
		}
	}
	
	enum LoggingFramework {
		// private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(TargetType.class);
		COMMONS(lombok.jul.Log.class, "org.apache.commons.logging.Log", "org.apache.commons.logging.LogFactory", "getLog"),
		
		// private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("TargetType");
		JUL(lombok.jul.Log.class, "java.util.logging.Logger", "java.util.logging.Logger", "getLogger") {
			@Override public Expression createFactoryParameter(String typeName, Annotation source) {
				Expression current = new StringLiteral(typeName.toCharArray(), source.sourceStart, source.sourceEnd, 0);
				Eclipse.setGeneratedBy(current, source);
				return current;
			}
		},
		
		// private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TargetType.class);
		LOG4J(lombok.jul.Log.class, "org.apache.log4j.Logger", "org.apache.log4j.Logger", "getLogger"),

		// private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TargetType.class);
		SLF4J(lombok.slf4j.Log.class, "org.slf4j.Logger", "org.slf4j.LoggerFactory", "getLogger"),
		
		;
		
		private final Class<? extends java.lang.annotation.Annotation> annotationClass;
		private final String loggerTypeName;
		private final String loggerFactoryTypeName;
		private final String loggerFactoryMethodName;

		LoggingFramework(Class<? extends java.lang.annotation.Annotation> annotationClass, String loggerTypeName, String loggerFactoryTypeName, String loggerFactoryMethodName) {
			this.annotationClass = annotationClass;
			this.loggerTypeName = loggerTypeName;
			this.loggerFactoryTypeName = loggerFactoryTypeName;
			this.loggerFactoryMethodName = loggerFactoryMethodName;
		}
		
		final Class<? extends java.lang.annotation.Annotation> getAnnotationClass() {
			return annotationClass;
		}
		
		final String getLoggerTypeName() {
			return loggerTypeName;
		}
		
		final String getLoggerFactoryTypeName() {
			return loggerFactoryTypeName;
		}
		
		final String getLoggerFactoryMethodName() {
			return loggerFactoryMethodName;
		}
		
		Expression createFactoryParameter(String typeName, Annotation source){
			return createNameReference(typeName + ".class", source);
		};
	}
}
