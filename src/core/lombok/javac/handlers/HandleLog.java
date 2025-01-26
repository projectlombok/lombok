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
package lombok.javac.handlers;

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.Javac.CTC_BOT;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import lombok.ConfigurationKeys;
import lombok.core.AnnotationValues;
import lombok.core.configuration.IdentifierName;
import lombok.core.configuration.LogDeclaration;
import lombok.core.configuration.LogDeclaration.LogFactoryParameter;
import lombok.core.handlers.LoggingFramework;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil.MemberExistsResult;
import lombok.spi.Provides;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

public class HandleLog {
	private static final IdentifierName LOG = IdentifierName.valueOf("log");
	
	private HandleLog() {
		throw new UnsupportedOperationException();
	}
	
	public static void processAnnotation(LoggingFramework framework, AnnotationValues<?> annotation, JavacNode annotationNode) {
		deleteAnnotationIfNeccessary(annotationNode, framework.getAnnotationClass());
		
		JavacNode typeNode = annotationNode.up();
		switch (typeNode.getKind()) {
		case TYPE:
			IdentifierName logFieldName = annotationNode.getAst().readConfiguration(ConfigurationKeys.LOG_ANY_FIELD_NAME);
			if (logFieldName == null) logFieldName = LOG;
			
			boolean useStatic = !Boolean.FALSE.equals(annotationNode.getAst().readConfiguration(ConfigurationKeys.LOG_ANY_FIELD_IS_STATIC));
			
			if ((((JCClassDecl) typeNode.get()).mods.flags & Flags.INTERFACE) != 0) {
				annotationNode.addError(framework.getAnnotationAsString() + " is legal only on classes and enums.");
				return;
			}
			if (fieldExists(logFieldName.getName(), typeNode) != MemberExistsResult.NOT_EXISTS) {
				annotationNode.addWarning("Field '" + logFieldName + "' already exists.");
				return;
			}
			
			if (isRecord(typeNode) && !useStatic) {
				annotationNode.addError("Logger fields must be static in records.");
				return;
			}
			
			if (useStatic && !isStaticAllowed(typeNode)) {
				annotationNode.addError(framework.getAnnotationAsString() + " is not supported on non-static nested classes.");
				return;
			}
			
			Object valueGuess = annotation.getValueGuess("topic");
			JCExpression loggerTopic = (JCExpression) annotation.getActualExpression("topic");
			
			if (valueGuess instanceof String && ((String) valueGuess).trim().isEmpty()) loggerTopic = null;
			if (framework.getDeclaration().getParametersWithTopic() == null && loggerTopic != null) {
				annotationNode.addError(framework.getAnnotationAsString() + " does not allow a topic.");
				loggerTopic = null;
			}
			if (framework.getDeclaration().getParametersWithoutTopic() == null && loggerTopic == null) {
				annotationNode.addError(framework.getAnnotationAsString() + " requires a topic.");
				loggerTopic = typeNode.getTreeMaker().Literal("");
			}
			
			JCFieldAccess loggingType = selfType(typeNode);
			createField(framework, typeNode, loggingType, annotationNode, logFieldName.getName(), useStatic, loggerTopic);
			break;
		default:
			annotationNode.addError("@Log is legal only on types.");
			break;
		}
	}
	
	public static JCFieldAccess selfType(JavacNode typeNode) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		Name name = ((JCClassDecl) typeNode.get()).name;
		return maker.Select(maker.Ident(name), typeNode.toName("class"));
	}
	
	private static boolean createField(LoggingFramework framework, JavacNode typeNode, JCFieldAccess loggingType, JavacNode source, String logFieldName, boolean useStatic, JCExpression loggerTopic) {
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		LogDeclaration logDeclaration = framework.getDeclaration();
		// private static final <loggerType> log = <factoryMethod>(<parameter>);
		JCExpression loggerType = chainDotsString(typeNode, logDeclaration.getLoggerType().getName());
		JCExpression factoryMethod = chainDotsString(typeNode, logDeclaration.getLoggerFactoryType().getName() + "." + logDeclaration.getLoggerFactoryMethod().getName());
		
		java.util.List<LogFactoryParameter> parameters = loggerTopic != null ? logDeclaration.getParametersWithTopic() : logDeclaration.getParametersWithoutTopic();
		JCExpression[] factoryParameters = createFactoryParameters(typeNode, loggingType, parameters, loggerTopic);
		JCMethodInvocation factoryMethodCall = maker.Apply(List.<JCExpression>nil(), factoryMethod, List.<JCExpression>from(factoryParameters));
		
		JCVariableDecl fieldDecl = recursiveSetGeneratedBy(maker.VarDef(
			maker.Modifiers(Flags.PRIVATE | Flags.FINAL | (useStatic ? Flags.STATIC : 0)),
			typeNode.toName(logFieldName), loggerType, factoryMethodCall), source);
		
		if (isRecord(typeNode) && Javac.getJavaCompilerVersion() < 16) {
			// This is a workaround for https://bugs.openjdk.java.net/browse/JDK-8243057 - note that the workaround is only for static fields in records, but our infra _requires_ `static` for logs in records (think about it).
			
			injectField(typeNode, fieldDecl);
		} else {
			injectFieldAndMarkGenerated(typeNode, fieldDecl);
		}
		
		return true;
	}
	
	private static JCExpression[] createFactoryParameters(JavacNode typeNode, JCFieldAccess loggingType, java.util.List<LogFactoryParameter> parameters, JCExpression loggerTopic) {
		JCExpression[] expressions = new JCExpression[parameters.size()];
		JavacTreeMaker maker = typeNode.getTreeMaker();
		
		for (int i = 0; i < parameters.size(); i++) {
			LogFactoryParameter parameter = parameters.get(i);
			switch (parameter) {
			case TYPE:
				expressions[i] = cloneType(maker, loggingType, typeNode);
				break;
			case NAME:
				JCExpression method = maker.Select(loggingType, typeNode.toName("getName"));
				expressions[i] = maker.Apply(List.<JCExpression>nil(), method, List.<JCExpression>nil());
				break;
			case TOPIC:
				if (loggerTopic instanceof JCLiteral) {
					expressions[i] = maker.Literal(((JCLiteral) loggerTopic).value);
				} else {
					expressions[i] = cloneType(maker, loggerTopic, typeNode);
				}
				break;
			case NULL:
				expressions[i] = maker.Literal(CTC_BOT, null);
				break;
			default:
				throw new IllegalStateException("Unknown logger factory parameter type: " + parameter);
			}
		}
		
		return expressions;
	}
	
	/**
	 * Handles the {@link lombok.extern.apachecommons.CommonsLog} annotation for javac.
	 */
	@Provides
	public static class HandleCommonsLog extends JavacAnnotationHandler<lombok.extern.apachecommons.CommonsLog> {
		@Override public void handle(AnnotationValues<lombok.extern.apachecommons.CommonsLog> annotation, JCAnnotation ast, JavacNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_COMMONS_FLAG_USAGE, "@apachecommons.CommonsLog", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			processAnnotation(LoggingFramework.COMMONS, annotation, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.extern.java.Log} annotation for javac.
	 */
	@Provides
	public static class HandleJulLog extends JavacAnnotationHandler<lombok.extern.java.Log> {
		@Override public void handle(AnnotationValues<lombok.extern.java.Log> annotation, JCAnnotation ast, JavacNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_JUL_FLAG_USAGE, "@java.Log", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			processAnnotation(LoggingFramework.JUL, annotation, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.extern.log4j.Log4j} annotation for javac.
	 */
	@Provides
	public static class HandleLog4jLog extends JavacAnnotationHandler<lombok.extern.log4j.Log4j> {
		@Override public void handle(AnnotationValues<lombok.extern.log4j.Log4j> annotation, JCAnnotation ast, JavacNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_LOG4J_FLAG_USAGE, "@Log4j", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			processAnnotation(LoggingFramework.LOG4J, annotation, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.extern.log4j.Log4j2} annotation for javac.
	 */
	@Provides
	public static class HandleLog4j2Log extends JavacAnnotationHandler<lombok.extern.log4j.Log4j2> {
		@Override public void handle(AnnotationValues<lombok.extern.log4j.Log4j2> annotation, JCAnnotation ast, JavacNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_LOG4J2_FLAG_USAGE, "@Log4j2", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			processAnnotation(LoggingFramework.LOG4J2, annotation, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.extern.slf4j.Slf4j} annotation for javac.
	 */
	@Provides
	public static class HandleSlf4jLog extends JavacAnnotationHandler<lombok.extern.slf4j.Slf4j> {
		@Override public void handle(AnnotationValues<lombok.extern.slf4j.Slf4j> annotation, JCAnnotation ast, JavacNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_SLF4J_FLAG_USAGE, "@Slf4j", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			processAnnotation(LoggingFramework.SLF4J, annotation, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.extern.slf4j.XSlf4j} annotation for javac.
	 */
	@Provides
	public static class HandleXSlf4jLog extends JavacAnnotationHandler<lombok.extern.slf4j.XSlf4j> {
		@Override public void handle(AnnotationValues<lombok.extern.slf4j.XSlf4j> annotation, JCAnnotation ast, JavacNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_XSLF4J_FLAG_USAGE, "@XSlf4j", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			processAnnotation(LoggingFramework.XSLF4J, annotation, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.extern.jbosslog.JBossLog} annotation for javac.
	 */
	@Provides
	public static class HandleJBossLog extends JavacAnnotationHandler<lombok.extern.jbosslog.JBossLog> {
		@Override public void handle(AnnotationValues<lombok.extern.jbosslog.JBossLog> annotation, JCAnnotation ast, JavacNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_JBOSSLOG_FLAG_USAGE, "@JBossLog", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			processAnnotation(LoggingFramework.JBOSSLOG, annotation, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.extern.flogger.Flogger} annotation for javac.
	 */
	@Provides
	public static class HandleFloggerLog extends JavacAnnotationHandler<lombok.extern.flogger.Flogger> {
		@Override public void handle(AnnotationValues<lombok.extern.flogger.Flogger> annotation, JCAnnotation ast, JavacNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_FLOGGER_FLAG_USAGE, "@Flogger", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			processAnnotation(LoggingFramework.FLOGGER, annotation, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.CustomLog} annotation for javac.
	 */
	@Provides
	public static class HandleCustomLog extends JavacAnnotationHandler<lombok.CustomLog> {
		@Override public void handle(AnnotationValues<lombok.CustomLog> annotation, JCAnnotation ast, JavacNode annotationNode) {
			handleFlagUsage(annotationNode, ConfigurationKeys.LOG_CUSTOM_FLAG_USAGE, "@CustomLog", ConfigurationKeys.LOG_ANY_FLAG_USAGE, "any @Log");
			LogDeclaration logDeclaration = annotationNode.getAst().readConfiguration(ConfigurationKeys.LOG_CUSTOM_DECLARATION);
			if (logDeclaration == null) {
				annotationNode.addError("The @CustomLog is not configured; please set lombok.log.custom.declaration in lombok.config.");
				return;
			}
			LoggingFramework framework = new LoggingFramework(lombok.CustomLog.class, logDeclaration);
			processAnnotation(framework, annotation, annotationNode);
		}
	}
}
