/*
 * Copyright (C) 2010-2021 The Project Lombok Authors.
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

import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.lang.reflect.Modifier;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import lombok.ConfigurationKeys;
import lombok.core.AnnotationValues;
import lombok.core.configuration.IdentifierName;
import lombok.core.configuration.LogDeclaration;
import lombok.core.configuration.LogDeclaration.LogFactoryParameter;
import lombok.core.handlers.LoggingFramework;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.MemberExistsResult;
import lombok.spi.Provides;

public class HandleLog {
	private static final IdentifierName LOG = IdentifierName.valueOf("log");
	
	private HandleLog() {
		throw new UnsupportedOperationException();
	}
	
	public static void processAnnotation(LoggingFramework framework, AnnotationValues<? extends java.lang.annotation.Annotation> annotation, Annotation source, EclipseNode annotationNode) {
		EclipseNode owner = annotationNode.up();
		
		switch (owner.getKind()) {
		case TYPE:
			IdentifierName logFieldName = annotationNode.getAst().readConfiguration(ConfigurationKeys.LOG_ANY_FIELD_NAME);
			if (logFieldName == null) logFieldName = LOG;
			
			boolean useStatic = !Boolean.FALSE.equals(annotationNode.getAst().readConfiguration(ConfigurationKeys.LOG_ANY_FIELD_IS_STATIC));
			
			TypeDeclaration typeDecl = null;
			if (owner.get() instanceof TypeDeclaration) typeDecl = (TypeDeclaration) owner.get();
			int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
			
			boolean notAClass = (modifiers & (ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation)) != 0;
			
			if (typeDecl == null || notAClass) {
				annotationNode.addError(framework.getAnnotationAsString() + " is legal only on classes and enums.");
				return;
			}
			
			if (fieldExists(logFieldName.getName(), owner) != MemberExistsResult.NOT_EXISTS) {
				annotationNode.addWarning("Field '" + logFieldName + "' already exists.");
				return;
			}
			
			if (isRecord(owner) && !useStatic) {
				annotationNode.addError("Logger fields must be static in records.");
				return;
			}
			
			if (useStatic && !isStaticAllowed(owner)) {
				annotationNode.addError(framework.getAnnotationAsString() + " is not supported on non-static nested classes.");
				return;
			}
			
			Object valueGuess = annotation.getValueGuess("topic");
			Expression loggerTopic = (Expression) annotation.getActualExpression("topic");
			
			if (valueGuess instanceof String && ((String) valueGuess).trim().isEmpty()) loggerTopic = null;
			if (framework.getDeclaration().getParametersWithTopic() == null && loggerTopic != null) {
				annotationNode.addError(framework.getAnnotationAsString() + " does not allow a topic.");
				loggerTopic = null;
			}
			if (framework.getDeclaration().getParametersWithoutTopic() == null && loggerTopic == null) {
				annotationNode.addError(framework.getAnnotationAsString() + " requires a topic.");
				loggerTopic = new StringLiteral(new char[]{}, 0, 0, 0);
			}
			
			ClassLiteralAccess loggingType = selfType(owner, source);
			FieldDeclaration fieldDeclaration = createField(framework, source, loggingType, logFieldName.getName(), useStatic, loggerTopic);
			fieldDeclaration.traverse(new SetGeneratedByVisitor(source), typeDecl.staticInitializerScope);
			// TODO temporary workaround for issue 290. https://github.com/projectlombok/lombok/issues/290
			// injectFieldSuppressWarnings(owner, fieldDeclaration);
			injectField(owner, fieldDeclaration);
			owner.rebuild();
			break;
		default:
			break;
		}
	}
	
	public static ClassLiteralAccess selfType(EclipseNode type, Annotation source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		
		TypeDeclaration typeDeclaration = (TypeDeclaration)type.get();
		TypeReference typeReference = new SingleTypeReference(typeDeclaration.name, p);
		setGeneratedBy(typeReference, source);
		
		ClassLiteralAccess result = new ClassLiteralAccess(source.sourceEnd, typeReference);
		setGeneratedBy(result, source);
		
		return result;
	}
	
	private static FieldDeclaration createField(LoggingFramework framework, Annotation source, ClassLiteralAccess loggingType, String logFieldName, boolean useStatic, Expression loggerTopic) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		
		// private static final <loggerType> log = <factoryMethod>(<parameter>);
		FieldDeclaration fieldDecl = new FieldDeclaration(logFieldName.toCharArray(), 0, -1);
		setGeneratedBy(fieldDecl, source);
		fieldDecl.declarationSourceEnd = -1;
		fieldDecl.modifiers = Modifier.PRIVATE | (useStatic ? Modifier.STATIC : 0) | Modifier.FINAL;
		
		LogDeclaration logDeclaration = framework.getDeclaration();
		fieldDecl.type = createTypeReference(logDeclaration.getLoggerType().getName(), source);
		
		MessageSend factoryMethodCall = new MessageSend();
		setGeneratedBy(factoryMethodCall, source);
		
		factoryMethodCall.receiver = createNameReference(logDeclaration.getLoggerFactoryType().getName(), source);
		factoryMethodCall.selector = logDeclaration.getLoggerFactoryMethod().getCharArray();
		
		List<LogFactoryParameter> parameters = loggerTopic != null ? logDeclaration.getParametersWithTopic() : logDeclaration.getParametersWithoutTopic();
		factoryMethodCall.arguments = createFactoryParameters(loggingType, source, parameters, loggerTopic);
		factoryMethodCall.nameSourcePosition = p;
		factoryMethodCall.sourceStart = pS;
		factoryMethodCall.sourceEnd = factoryMethodCall.statementEnd = pE;
		
		fieldDecl.initialization = factoryMethodCall;
		
		return fieldDecl;
	}
	
	private static final Expression[] createFactoryParameters(ClassLiteralAccess loggingType, Annotation source, List<LogFactoryParameter> parameters, Expression loggerTopic) {
		Expression[] expressions = new Expression[parameters.size()];
		int pS = source.sourceStart, pE = source.sourceEnd;
		
		for (int i = 0; i < parameters.size(); i++) {
			LogFactoryParameter parameter = parameters.get(i);
			
			switch(parameter) {
			case TYPE:
				expressions[i] = createFactoryTypeParameter(loggingType, source);
				break;
			case NAME:
				long p = (long) pS << 32 | pE;
				
				MessageSend factoryParameterCall = new MessageSend();
				setGeneratedBy(factoryParameterCall, source);
				
				factoryParameterCall.receiver = createFactoryTypeParameter(loggingType, source);
				factoryParameterCall.selector = "getName".toCharArray();
				
				factoryParameterCall.nameSourcePosition = p;
				factoryParameterCall.sourceStart = pS;
				factoryParameterCall.sourceEnd = factoryParameterCall.statementEnd = pE;
				
				expressions[i] = factoryParameterCall;
				break;
			case TOPIC:
				expressions[i] = EclipseHandlerUtil.copyAnnotationMemberValue(loggerTopic);
				break;
			case NULL:
				expressions[i] = new NullLiteral(pS, pE);
				break;
			default:
				throw new IllegalStateException("Unknown logger factory parameter type: " + parameter);
			}
		}
		
		return expressions;
	}
	
	private static final Expression createFactoryTypeParameter(ClassLiteralAccess loggingType, Annotation source) {
		TypeReference copy = copyType(loggingType.type, source);
		ClassLiteralAccess result = new ClassLiteralAccess(source.sourceEnd, copy);
		setGeneratedBy(result, source);
		return result;
	}
	
	/**
	 * Handles the {@link lombok.extern.apachecommons.CommonsLog} annotation for Eclipse.
	 */
	@Provides
	public static class HandleCommonsLog extends EclipseAnnotationHandler<lombok.extern.apachecommons.CommonsLog> {
		@Override public void handle(AnnotationValues<lombok.extern.apachecommons.CommonsLog> annotation, Annotation source, EclipseNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_COMMONS_FLAG_USAGE, "@apachecommons.CommonsLog", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			processAnnotation(LoggingFramework.COMMONS, annotation, source, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.extern.java.Log} annotation for Eclipse.
	 */
	@Provides
	public static class HandleJulLog extends EclipseAnnotationHandler<lombok.extern.java.Log> {
		@Override public void handle(AnnotationValues<lombok.extern.java.Log> annotation, Annotation source, EclipseNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_JUL_FLAG_USAGE, "@java.Log", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			processAnnotation(LoggingFramework.JUL, annotation, source, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.extern.log4j.Log4j} annotation for Eclipse.
	 */
	@Provides
	public static class HandleLog4jLog extends EclipseAnnotationHandler<lombok.extern.log4j.Log4j> {
		@Override public void handle(AnnotationValues<lombok.extern.log4j.Log4j> annotation, Annotation source, EclipseNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_LOG4J_FLAG_USAGE, "@Log4j", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			processAnnotation(LoggingFramework.LOG4J, annotation, source, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.extern.log4j.Log4j2} annotation for Eclipse.
	 */
	@Provides
	public static class HandleLog4j2Log extends EclipseAnnotationHandler<lombok.extern.log4j.Log4j2> {
		@Override public void handle(AnnotationValues<lombok.extern.log4j.Log4j2> annotation, Annotation source, EclipseNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_LOG4J2_FLAG_USAGE, "@Log4j2", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			processAnnotation(LoggingFramework.LOG4J2, annotation, source, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.extern.slf4j.Slf4j} annotation for Eclipse.
	 */
	@Provides
	public static class HandleSlf4jLog extends EclipseAnnotationHandler<lombok.extern.slf4j.Slf4j> {
		@Override public void handle(AnnotationValues<lombok.extern.slf4j.Slf4j> annotation, Annotation source, EclipseNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_SLF4J_FLAG_USAGE, "@Slf4j", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			processAnnotation(LoggingFramework.SLF4J, annotation, source, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.extern.slf4j.XSlf4j} annotation for Eclipse.
	 */
	@Provides
	public static class HandleXSlf4jLog extends EclipseAnnotationHandler<lombok.extern.slf4j.XSlf4j> {
		@Override public void handle(AnnotationValues<lombok.extern.slf4j.XSlf4j> annotation, Annotation source, EclipseNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_XSLF4J_FLAG_USAGE, "@XSlf4j", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			processAnnotation(LoggingFramework.XSLF4J, annotation, source, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.extern.jbosslog.JBossLog} annotation for Eclipse.
	 */
	@Provides
	public static class HandleJBossLog extends EclipseAnnotationHandler<lombok.extern.jbosslog.JBossLog> {
		@Override public void handle(AnnotationValues<lombok.extern.jbosslog.JBossLog> annotation, Annotation source, EclipseNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_JBOSSLOG_FLAG_USAGE, "@JBossLog", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			processAnnotation(LoggingFramework.JBOSSLOG, annotation, source, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.extern.flogger.Flogger} annotation for Eclipse.
	 */
	@Provides
	public static class HandleFloggerLog extends EclipseAnnotationHandler<lombok.extern.flogger.Flogger> {
		@Override public void handle(AnnotationValues<lombok.extern.flogger.Flogger> annotation, Annotation source, EclipseNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_FLOGGER_FLAG_USAGE, "@Flogger", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			processAnnotation(LoggingFramework.FLOGGER, annotation, source, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.CustomLog} annotation for Eclipse.
	 */
	@Provides
	public static class HandleCustomLog extends EclipseAnnotationHandler<lombok.CustomLog> {
		@Override public void handle(AnnotationValues<lombok.CustomLog> annotation, Annotation source, EclipseNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_CUSTOM_FLAG_USAGE, "@CustomLog", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			LogDeclaration logDeclaration = annotationNode.getAst().readConfiguration(ConfigurationKeys.LOG_CUSTOM_DECLARATION);
			if (logDeclaration == null) {
				annotationNode.addError("The @CustomLog annotation is not configured; please set lombok.log.custom.declaration in lombok.config.");
				return;
			}
			LoggingFramework framework = new LoggingFramework(lombok.CustomLog.class, logDeclaration);
			processAnnotation(framework, annotation, source, annotationNode);
		}
	}
}
