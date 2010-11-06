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
			createField(typeNode, loggingClassName, framework);
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
	
	private static boolean createField(JavacNode typeNode, String loggerClassName, LoggingFramework framework) {
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
	
	static interface LoggingFramework {
		Class<? extends Annotation> getAnnotationClass();
		String getLoggerTypeName();
		String getLoggerFactoryMethodName();
		JCExpression createFactoryParameter(JavacNode typeNode, String typeName);
	}
	
	// private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(TargetType.class);
	private static LoggingFramework COMMONS = new LoggingFramework() {
		@Override public Class<? extends Annotation> getAnnotationClass() {
			return lombok.jul.Log.class;
		}
		
		@Override public String getLoggerTypeName() {
			return "org.apache.commons.logging.Log";
		}
		
		@Override public String getLoggerFactoryMethodName() {
			return "org.apache.commons.logging.LogFactory.getLog";
		}
		
		@Override public JCExpression createFactoryParameter(JavacNode typeNode, String typeName) {
			return chainDotsString(typeNode.getTreeMaker(), typeNode, typeName + ".class");
		}
	};
	
	/**
	 * Handles the {@link lombok.commons.Log} annotation for javac.
	 */
	@ProviderFor(JavacAnnotationHandler.class)
	public static class HandleCommonsLog implements JavacAnnotationHandler<lombok.commons.Log> {
		@Override public boolean handle(AnnotationValues<lombok.commons.Log> annotation, JCAnnotation ast, JavacNode annotationNode) {
			return processAnnotation(COMMONS, annotation, annotationNode);
		}
	}
	
	// private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("TargetType");
	private static LoggingFramework JUL = new LoggingFramework() {
		@Override public Class<? extends Annotation> getAnnotationClass() {
			return lombok.jul.Log.class;
		}
		
		@Override public String getLoggerTypeName() {
			return "java.util.logging.Logger";
		}
		
		@Override public String getLoggerFactoryMethodName() {
			return "java.util.logging.Logger.getLogger";
		}
		
		@Override public JCExpression createFactoryParameter(JavacNode typeNode, String typeName) {
			return typeNode.getTreeMaker().Literal(typeName);
		}
	};
	
	/**
	 * Handles the {@link lombok.jul.Log} annotation for javac.
	 */
	@ProviderFor(JavacAnnotationHandler.class)
	public static class HandleJulLog implements JavacAnnotationHandler<lombok.jul.Log> {
		@Override public boolean handle(AnnotationValues<lombok.jul.Log> annotation, JCAnnotation ast, JavacNode annotationNode) {
			return processAnnotation(JUL, annotation, annotationNode);
		}
	}	
	
	// private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TargetType.class);
	private static LoggingFramework LOG4J = new LoggingFramework() {
		@Override public Class<? extends Annotation> getAnnotationClass() {
			return lombok.jul.Log.class;
		}
		
		@Override public String getLoggerTypeName() {
			return "org.apache.log4j.Logger";
		}
		
		@Override public String getLoggerFactoryMethodName() {
			return "org.apache.log4j.Logger.getLogger";
		}
		
		@Override public JCExpression createFactoryParameter(JavacNode typeNode, String typeName) {
			return chainDotsString(typeNode.getTreeMaker(), typeNode, typeName + ".class");
		}
	};
	
	/**
	 * Handles the {@link lombok.log4j.Log} annotation for javac.
	 */
	@ProviderFor(JavacAnnotationHandler.class)
	public static class HandleLog4jLog implements JavacAnnotationHandler<lombok.log4j.Log> {
		@Override public boolean handle(AnnotationValues<lombok.log4j.Log> annotation, JCAnnotation ast, JavacNode annotationNode) {
			return processAnnotation(LOG4J, annotation, annotationNode);
		}
	}
	
	// private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TargetType.class);
	private static LoggingFramework SLF4J = new LoggingFramework() {
		@Override public Class<? extends Annotation> getAnnotationClass() {
			return lombok.slf4j.Log.class;
		}
		
		@Override public String getLoggerTypeName() {
			return "org.slf4j.Logger";
		}
		
		@Override public String getLoggerFactoryMethodName() {
			return "org.slf4j.LoggerFactory.getLogger";
		}
		
		@Override public JCExpression createFactoryParameter(JavacNode typeNode, String typeName) {
			return chainDotsString(typeNode.getTreeMaker(), typeNode, typeName + ".class");
		}
	};
	
	/**
	 * Handles the {@link lombok.slf4j.Log} annotation for javac.
	 */
	@ProviderFor(JavacAnnotationHandler.class)
	public static class HandleSlf4jLog implements JavacAnnotationHandler<lombok.slf4j.Log> {
		@Override public boolean handle(AnnotationValues<lombok.slf4j.Log> annotation, JCAnnotation ast, JavacNode annotationNode) {
			return processAnnotation(SLF4J, annotation, annotationNode);
		}
	}
}
