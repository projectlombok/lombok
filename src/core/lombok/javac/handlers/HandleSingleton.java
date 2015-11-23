/*
 * Copyright (C) 2015 The Project Lombok Authors.
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
import static lombok.javac.handlers.JavacHandlerUtil.injectField;
import static lombok.javac.handlers.JavacHandlerUtil.injectMethod;
import static lombok.javac.handlers.JavacHandlerUtil.injectType;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.mangosdk.spi.ProviderFor;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.experimental.Singleton;
import lombok.experimental.Tolerate;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.HandleLog.LoggingFramework;
import lombok.javac.handlers.JavacHandlerUtil.FieldAccess;
import lombok.javac.handlers.JavacHandlerUtil.MemberExistsResult;

@ProviderFor(JavacAnnotationHandler.class)
public class HandleSingleton extends JavacAnnotationHandler<Singleton> {
	@Override public void handle(AnnotationValues<Singleton> annotation, JCAnnotation ast, JavacNode annotationNode) {
		handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.SINGLETON_FLAG_USAGE, "@Singleton");
		
		deleteAnnotationIfNeccessary(annotationNode, Singleton.class);
		deleteImportFromCompilationUnit(annotationNode, "lombok.Singleton.Style");
		
		JavacNode typeNode = annotationNode.up();
		if (typeNode == null) return;
		if (typeNode.getKind() != Kind.TYPE) {
		  annotationNode.addError("@Singleton is legal only on classes.");
		  return;
		}
		
		JCClassDecl type = null;
		if (typeNode.get() instanceof JCClassDecl) type = (JCClassDecl) typeNode.get();
		long modifiers = type == null ? 0 : type.mods.flags;
		boolean notAClass = (modifiers & (Flags.INTERFACE | Flags.ANNOTATION | Flags.ENUM)) != 0;
		
		if (type == null || notAClass) {
		  annotationNode.addError("@Singleton is only supported on a class.");
		  return;
		}
		
		String staticGetterName = annotationNode.getAst().readConfiguration(ConfigurationKeys.SINGLETON_STATIC_GETTER_NAME);
    if (staticGetterName == null) staticGetterName = "getInstance";
    MemberExistsResult getterExistsResult = methodExists(staticGetterName, typeNode, true, 0);
    if (getterExistsResult != MemberExistsResult.NOT_EXISTS) {
      annotationNode.addWarning("The static instance getter method: '" + staticGetterName + "' already exists.");
      return;
    }
		
    Singleton singleton = annotation.getInstance();
    Singleton.Style style = singleton.style();
    
		switch (style) {
		case EAGER:
		  handleEagerInstantiation(ast, typeNode, annotationNode, type, staticGetterName);
		  break;
		case LAZY:
		  handleLazyInstantiation(ast, typeNode, annotationNode, type, staticGetterName);
		  break;
		}
	}
	
	/**
   * Handles a request to eagerly instantiate the Singleton class by creating
   * a field and assigning it an object by calling the nullary constructor.
   * 
   * @param source  the {@code AST} setup
   * @param typeNode  the declaring type
   * @param annotationNode  the annotation node to declare warnings on
   * @param type  the enclosing type declaration
   * @param staticGetterName  the getter of the instance
   */
  private void handleEagerInstantiation(JCAnnotation source, JavacNode typeNode, JavacNode annotationNode,
      JCClassDecl type, String staticGetterName) {
    String instanceFieldName = annotationNode.getAst().readConfiguration(ConfigurationKeys.SINGLETON_INSTANCE_FIELD_NAME);
    if (instanceFieldName == null) instanceFieldName = "INSTANCE";
    if (!instanceFieldName.isEmpty()) {
      if (!checkName("instanceFieldName", instanceFieldName, annotationNode)) return;
    }
    
    MemberExistsResult instanceFieldExists = fieldExists(instanceFieldName, typeNode);
    if (instanceFieldExists != MemberExistsResult.NOT_EXISTS) {
      annotationNode.addWarning("The instance field '" + instanceFieldName + "' already exists.");
      return;
    }
    
    JCTree tree = annotationNode.get();
    JCVariableDecl fieldDecl = createField(typeNode, tree, type, instanceFieldName);
    JavacNode fieldNode = injectFieldAndMarkGenerated(typeNode, fieldDecl);
    
    JCMethodDecl getterMethodDeclaration = generateGetterMethod(type, staticGetterName, fieldNode.getTreeMaker(), tree, fieldNode);
    injectMethod(fieldNode.up(), getterMethodDeclaration);
  }
  
  private static JCVariableDecl createField(JavacNode typeNode, JCTree source, JCClassDecl type, String instanceFieldName) {
    JavacTreeMaker treeMaker = typeNode.getTreeMaker();
    
    JCExpression init = treeMaker.NewClass(null, List.<JCExpression>nil(), treeMaker.Ident(type.name), List.<JCExpression>nil(), null);
    JCExpression varType = treeMaker.Ident(type.name);
    JCVariableDecl fieldDecl = treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE | Flags.FINAL | Flags.STATIC),
        typeNode.toName(instanceFieldName),
        varType,
        init);
    return fieldDecl;
  }

  private JCMethodDecl generateGetterMethod(JCClassDecl type, String staticGetterName, JavacTreeMaker treeMaker, JCTree source,
      JavacNode fieldNode) {
    long getterAccess = Flags.PUBLIC | Flags.STATIC;
    List<JCStatement> returnStatement = List.<JCStatement>of(treeMaker.Return(createFieldAccessor(treeMaker, fieldNode, FieldAccess.ALWAYS_FIELD)));
    JCBlock methodBody = treeMaker.Block(0, returnStatement);
    List<JCTypeParameter> methodGenericParams = List.nil();
    List<JCVariableDecl> parameters = List.nil();
    List<JCExpression> throwsClauses = List.nil();
    JCExpression annotationMethodDefaultValue = null;
    Name getterMethodName = fieldNode.toName(staticGetterName);
    JCExpression methodType = treeMaker.Ident(type.name);
    JCMethodDecl getterMethodDeclaration = recursiveSetGeneratedBy(treeMaker.MethodDef(treeMaker.Modifiers(getterAccess), getterMethodName, methodType,
        methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue), source, fieldNode.getContext());
    return getterMethodDeclaration;
  }
  
  private void handleLazyInstantiation(JCAnnotation source, JavacNode typeNode, JavacNode annotationNode,
      JCClassDecl type, String staticGetterName) {
    /*String holderClassName = annotationNode.getAst().readConfiguration(ConfigurationKeys.SINGLETON_HOLDER_CLASS_NAME);
    if (holderClassName == null) holderClassName = "SingletonHolder";
    if (!holderClassName.isEmpty()) {
      if (!checkName("holderClassName", holderClassName, annotationNode)) return;
    }
    
    JavacNode holderType = findInnerClass(typeNode, holderClassName);
    if (holderType == null) {
      holderType = makeHolderClass(source, typeNode, holderClassName);
      FieldDeclaration holderInstanceField = createField(source, "INSTANCE", typeNode);
      EclipseNode heldInstance = injectField(holderType, holderInstanceField);
      
      MethodDeclaration md = generateGetterMethod(staticGetterName, typeNode, type.typeParameters, source, holderClassName, heldInstance);
      injectMethod(typeNode, md);
      typeNode.rebuild();
    } else {
      annotationNode.addWarning("The Singleton holder inner class '" + holderClassName + "' already exists.");
      return;
    }*/
  }
  
  /**
   * Makes a new holder (a static, final, inner) class to hold the Singleton
   * reference.
   * 
   * @param ast  the {@code AST} setup
   * @param parent  the enclosing node
   * @param holderClassName  the holder class name
   * @return a new holder (a static, final, inner) class to hold the Singleton
   * reference
   */
  private JavacNode makeHolderClass(JCAnnotation ast, JavacNode parent, String holderClassName) {
    /*JCClassDecl parentTypeDecl = (JCClassDecl) parent.get();
    JCClassDecl holder = new JCClassDecl(parentTypeDecl.compilationResult);
    
    
    return injectType(parent, holder);*/
    return null;
  }
  
  /**
   * Finds an inner class with the given name.
   * 
   * @param parent  the enclosing node
   * @param name  the name of the inner class
   * @return  the inner class with the given name; {@code null}
   * if none was found
   */
  private JavacNode findInnerClass(JavacNode parent, String name) {
    char[] c = name.toCharArray();
    for (JavacNode child : parent.down()) {
      if (child.getKind() != Kind.TYPE) continue;
      JCClassDecl td = (JCClassDecl) child.get();
      if (Arrays.equals(td.name.toString().toCharArray(), c)) return child;
    }
    return null;
  }
  
}
