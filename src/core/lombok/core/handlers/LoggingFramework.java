/*
 * Copyright (C) 2019 The Project Lombok Authors.
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
package lombok.core.handlers;

import java.lang.annotation.Annotation;

import lombok.core.configuration.LogDeclaration;

public class LoggingFramework {
	// private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(TargetType.class);
	public static final LoggingFramework COMMONS = new LoggingFramework(
		lombok.extern.apachecommons.CommonsLog.class,
		LogDeclaration.valueOf("org.apache.commons.logging.Log org.apache.commons.logging.LogFactory.getLog(TYPE)(TOPIC)")
	);
	
	// private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(TargetType.class.getName());
	public static final LoggingFramework JUL = new LoggingFramework(
		lombok.extern.java.Log.class,
		LogDeclaration.valueOf("java.util.logging.Logger java.util.logging.Logger.getLogger(NAME)(TOPIC)")
	);
	
	// private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TargetType.class);
	public static final LoggingFramework LOG4J = new LoggingFramework(
		lombok.extern.log4j.Log4j.class,
		LogDeclaration.valueOf("org.apache.log4j.Logger org.apache.log4j.Logger.getLogger(TYPE)(TOPIC)")
	);

	// private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(TargetType.class);
	public static final LoggingFramework LOG4J2 = new LoggingFramework(
		lombok.extern.log4j.Log4j2.class,
		LogDeclaration.valueOf("org.apache.logging.log4j.Logger org.apache.logging.log4j.LogManager.getLogger(TYPE)(TOPIC)")
	);

	// private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TargetType.class);
	public static final LoggingFramework SLF4J = new LoggingFramework(
		lombok.extern.slf4j.Slf4j.class,
		LogDeclaration.valueOf("org.slf4j.Logger org.slf4j.LoggerFactory.getLogger(TYPE)(TOPIC)")
	);
	
	// private static final org.slf4j.ext.XLogger log = org.slf4j.ext.XLoggerFactory.getXLogger(TargetType.class);
	public static final LoggingFramework XSLF4J = new LoggingFramework(
		lombok.extern.slf4j.XSlf4j.class,
		LogDeclaration.valueOf("org.slf4j.ext.XLogger org.slf4j.ext.XLoggerFactory.getXLogger(TYPE)(TOPIC)")
	);

	// private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(TargetType.class);
	public static final LoggingFramework JBOSSLOG = new LoggingFramework(
		lombok.extern.jbosslog.JBossLog.class,
		LogDeclaration.valueOf("org.jboss.logging.Logger org.jboss.logging.Logger.getLogger(TYPE)(TOPIC)")
	);
	
	// private static final com.google.common.flogger.FluentLogger log = com.google.common.flogger.FluentLogger.forEnclosingClass();
	public static final LoggingFramework FLOGGER = new LoggingFramework(
		lombok.extern.flogger.Flogger.class,
		LogDeclaration.valueOf("com.google.common.flogger.FluentLogger com.google.common.flogger.FluentLogger.forEnclosingClass()")
	);
	
	private final Class<? extends Annotation> annotationClass;
	private final String annotationAsString;
	private final LogDeclaration declaration;
	
	public LoggingFramework(Class<? extends Annotation> annotationClass, LogDeclaration declaration) {
		this.annotationClass = annotationClass;
		this.annotationAsString = "@" + annotationClass.getSimpleName();
		this.declaration = declaration;
	}
	
	public Class<? extends Annotation> getAnnotationClass() {
		return annotationClass;
	}
	
	public String getAnnotationAsString() {
		return annotationAsString;
	}
	
	public LogDeclaration getDeclaration() {
		return declaration;
	}
}
