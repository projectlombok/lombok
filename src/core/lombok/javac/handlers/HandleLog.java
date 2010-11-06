/*
 * Copyright © 2010 Reinier Zwitserloot, Roel Spilker and Robbert Jan Grootjans.
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

import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.lang.annotation.Annotation;

import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;

public class HandleLog {
	
	private HandleLog() {
		throw new UnsupportedOperationException();
	}
	
	public static boolean processAnnotation(LoggingFramework framework, AnnotationValues<?> annotation, JavacNode annotationNode) {
		markAnnotationAsProcessed(annotationNode, framework.getAnnotationClass());
		
		String loggingClassName = annotation.getRawExpression("value");
		if (loggingClassName == null) loggingClassName = "void";
		if (loggingClassName.endsWith(".class")) loggingClassName = loggingClassName.substring(0, loggingClassName.length() - 6);
		
		JavacNode typeNode = annotationNode.up();
		switch (typeNode.getKind()) {
		case TYPE:
			if ((((JCClassDecl)typeNode.get()).mods.flags & Flags.INTERFACE)!= 0) {
				annotationNode.addError("@Log is legal only on classes and enums.");
				return true;
			}
			
			if (fieldExists("log", typeNode)!= MemberExistsResult.NOT_EXISTS) {
				annotationNode.addWarning("Field 'log' already exists.");
				return true;
			}

			if (loggingClassName.equals("void")) {
				loggingClassName = getSelfName(typeNode);
			}
			createField(framework, typeNode, loggingClassName);
			return true;
		default:
			annotationNode.addError("@Log is legal only on types.");
			return true;
		}
	}
	
	private static String getSelfName(JavacNode typeNode) {
		String typeName = ((JCClassDecl) typeNode.get()).name.toString();
		JavacNode upType = typeNode.up();
		while (upType.getKind() == Kind.TYPE) {
			typeName = ((JCClassDecl) upType.get()).name.toString() + "." + typeName;
			upType = upType.up();
		}
		
		String packageDeclaration = typeNode.getPackageDeclaration();
		if (packageDeclaration != null) {
			typeName = packageDeclaration + "." + typeName;
		}
		return typeName;
	}
	
	private static boolean createField(LoggingFramework framework, JavacNode typeNode, String loggerClassName) {
		TreeMaker maker = typeNode.getTreeMaker();
		
		// 	private static final <loggerType> log = <factoryMethod>(<parameter>);
		JCExpression loggerType = chainDotsString(maker, typeNode, framework.getLoggerTypeName());
		JCExpression factoryMethod = chainDotsString(maker, typeNode, framework.getLoggerFactoryMethodName());
		
		JCExpression loggerName = framework.createFactoryParameter(typeNode, loggerClassName);
		JCMethodInvocation factoryMethodCall = maker.Apply(List.<JCExpression>nil(), factoryMethod, List.<JCExpression>of(loggerName));
		
		JCVariableDecl fieldDecl = maker.VarDef(
				maker.Modifiers(Flags.PRIVATE | Flags.FINAL | Flags.STATIC),
				typeNode.toName("log"), loggerType, factoryMethodCall);
		
		injectField(typeNode, fieldDecl);
		return true;
	}
	
	/**
	 * Handles the {@link lombok.commons.Log} annotation for javac.
	 */
	@ProviderFor(JavacAnnotationHandler.class)
	public static class HandleCommonsLog implements JavacAnnotationHandler<lombok.commons.Log> {
		@Override public boolean handle(AnnotationValues<lombok.commons.Log> annotation, JCAnnotation ast, JavacNode annotationNode) {
			return processAnnotation(LoggingFramework.COMMONS, annotation, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.jul.Log} annotation for javac.
	 */
	@ProviderFor(JavacAnnotationHandler.class)
	public static class HandleJulLog implements JavacAnnotationHandler<lombok.jul.Log> {
		@Override public boolean handle(AnnotationValues<lombok.jul.Log> annotation, JCAnnotation ast, JavacNode annotationNode) {
			return processAnnotation(LoggingFramework.JUL, annotation, annotationNode);
		}
	}	
	
	/**
	 * Handles the {@link lombok.log4j.Log} annotation for javac.
	 */
	@ProviderFor(JavacAnnotationHandler.class)
	public static class HandleLog4jLog implements JavacAnnotationHandler<lombok.log4j.Log> {
		@Override public boolean handle(AnnotationValues<lombok.log4j.Log> annotation, JCAnnotation ast, JavacNode annotationNode) {
			return processAnnotation(LoggingFramework.LOG4J, annotation, annotationNode);
		}
	}
	
	/**
	 * Handles the {@link lombok.slf4j.Log} annotation for javac.
	 */
	@ProviderFor(JavacAnnotationHandler.class)
	public static class HandleSlf4jLog implements JavacAnnotationHandler<lombok.slf4j.Log> {
		@Override public boolean handle(AnnotationValues<lombok.slf4j.Log> annotation, JCAnnotation ast, JavacNode annotationNode) {
			return processAnnotation(LoggingFramework.SLF4J, annotation, annotationNode);
		}
	}
	
	enum LoggingFramework {
		// private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(TargetType.class);
		COMMONS(lombok.jul.Log.class, "org.apache.commons.logging.Log", "org.apache.commons.logging.LogFactory.getLog"),
		
		// private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("TargetType");
		JUL(lombok.jul.Log.class, "java.util.logging.Logger", "java.util.logging.Logger.getLogger") {
			@Override public JCExpression createFactoryParameter(JavacNode typeNode, String typeName) {
				return typeNode.getTreeMaker().Literal(typeName);
			}
		},
		
		// private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TargetType.class);
		LOG4J(lombok.jul.Log.class, "org.apache.log4j.Logger", "org.apache.log4j.Logger.getLogger"),
		
		// private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TargetType.class);
		SLF4J(lombok.slf4j.Log.class, "org.slf4j.Logger", "org.slf4j.LoggerFactory.getLogger"),
		
		;
		
		private final Class<? extends Annotation> annotationClass;
		private final String loggerTypeName;
		private final String loggerFactoryName;
		
		LoggingFramework(Class<? extends Annotation> annotationClass, String loggerTypeName, String loggerFactoryName) {
			this.annotationClass = annotationClass;
			this.loggerTypeName = loggerTypeName;
			this.loggerFactoryName = loggerFactoryName;
		}
		
		final Class<? extends Annotation> getAnnotationClass() {
			return annotationClass;
		}
		
		final String getLoggerTypeName() {
			return loggerTypeName;
		}
		
		final String getLoggerFactoryMethodName() {
			return loggerFactoryName;
		}
		
		JCExpression createFactoryParameter(JavacNode typeNode, String typeName) {
			return chainDotsString(typeNode.getTreeMaker(), typeNode, typeName + ".class");
		}
	}
}
