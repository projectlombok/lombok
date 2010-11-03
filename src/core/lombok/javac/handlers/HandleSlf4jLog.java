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
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.slf4j.Log;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;

/**
 * Handles the {@code lombok.slf4j.Log} annotation for javac.
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleSlf4jLog implements JavacAnnotationHandler<Log> {
	@Override public boolean handle(AnnotationValues<Log> annotation, JCAnnotation ast, JavacNode annotationNode) {
		markAnnotationAsProcessed(annotationNode, Log.class);
		
		String loggingClassName = annotation.getRawExpression("value");
		if (loggingClassName == null) loggingClassName = "void.class";
		if (!loggingClassName.endsWith(".class")) loggingClassName = loggingClassName + ".class";
		
		
		JavacNode owner = annotationNode.up();
		switch (owner.getKind()) {
		case TYPE:
			if ((((JCClassDecl)owner.get()).mods.flags & Flags.INTERFACE)!= 0) {
				annotationNode.addError("@Log is legal only on classes and enums.");
				return true;
			}
			
			if (loggingClassName.equals("void.class")) {
				loggingClassName = getSelfName(owner);
			}
			return handleType(annotationNode, loggingClassName);
		default:
			annotationNode.addError("@Log is legal only on types.");
			return true;
		}
	}
	
	private String getSelfName(JavacNode typeNode) {
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
		return typeName + ".class";
	}
	
	private boolean handleType(JavacNode annotation, String loggerClassName) {
		JavacNode typeNode = annotation.up();
		
		TreeMaker maker = typeNode.getTreeMaker();
		
		switch (fieldExists("log", typeNode)) {
		case NOT_EXISTS: 
			// 	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerLog4j.class);
			JCExpression loggerType = chainDots(maker, typeNode, "org", "slf4j", "Logger");
			JCExpression factoryMethod = chainDots(maker, typeNode, "org", "slf4j", "LoggerFactory", "getLogger");
			
			JCExpression loggerName = chainDots(maker, typeNode, loggerClassName.split("\\."));
			JCMethodInvocation factoryMethodCall = maker.Apply(List.<JCExpression>nil(), factoryMethod, List.<JCExpression>of(loggerName));
			
			JCVariableDecl fieldDecl = maker.VarDef(
					maker.Modifiers(Flags.PRIVATE | Flags.FINAL | Flags.STATIC),
					typeNode.toName("log"), loggerType, factoryMethodCall);
			
			injectField(typeNode, fieldDecl);
			return true;
		case EXISTS_BY_USER: 
			annotation.addWarning("Field 'log' already exists.");
		}
		
		return true;
	}
}
