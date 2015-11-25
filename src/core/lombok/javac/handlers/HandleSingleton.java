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
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.lang.reflect.Modifier;
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
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.core.AST.Kind;
import lombok.javac.handlers.HandleConstructor;
import lombok.javac.handlers.HandleConstructor.SkipIfConstructorExists;
import lombok.core.AnnotationValues;
import lombok.experimental.Singleton;
import lombok.experimental.Tolerate;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.HandleLog.LoggingFramework;
import lombok.javac.handlers.JavacHandlerUtil.FieldAccess;
import lombok.javac.handlers.JavacHandlerUtil.MemberExistsResult;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

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
		boolean isUsedOnClass = isAnnotationUsedOnClass(type, annotationNode);
		if (! isUsedOnClass) {
		  return;
		}
		
		if (nullaryConstructorExists(typeNode, annotationNode) == MemberExistsResult.NOT_EXISTS) {
      HandleConstructor constrHandler = new HandleConstructor();
      java.util.List<JavacNode> uninitializedFinalFields = uninitializedFinalFields(typeNode);
      if (uninitializedFinalFields.isEmpty()) {
        constrHandler.generateConstructor(typeNode, AccessLevel.PRIVATE, List.<JCAnnotation>nil(), List.<JavacNode>nil(), true, null, SkipIfConstructorExists.NO, false, annotationNode);
      } else {
        java.util.List<String> fieldNames = new ArrayList<String>();
        for (JavacNode field : uninitializedFinalFields) {
          fieldNames.add(field.getName());
        }
        annotationNode.addError("Could not create a nullary constructor. The final fields: " + fieldNames + " are not initialized.");
        return;
      }
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
		default:
		  annotationNode.addWarning("Invalid instantiation style: '" + staticGetterName + "'.");
		  break;
		}
	}
	
	private java.util.List<JavacNode> uninitializedFinalFields(JavacNode typeNode) {
	  java.util.List<JavacNode> fields = new ArrayList<JavacNode>();
    for (JavacNode child : typeNode.down()) {
      if (child.getKind() != Kind.FIELD) continue;
      JCVariableDecl fieldDecl = (JCVariableDecl) child.get();
      if (((fieldDecl.mods.flags & ClassFileConstants.AccFinal) != 0) && fieldDecl.init == null) {
        fields.add(child);
      }
    }
    return fields;
  }

	/**
   * Checks if the given type declaration corresponds to that of a class.
   * 
   * <p>
   * Also adds an error message to the given annotation node if the type declaration
   * is not that of a class.
   * 
   * @param type  the type declaration to check
   * @param annotationNode  the annotation node to add an error message to if the
   * given type declaration does not correspond to that of a class
   * @return  {@code true} if the given type declaration is that of a class; {@code false}
   * otherwise
   */
  boolean isAnnotationUsedOnClass(JCClassDecl type, JavacNode annotationNode) {
    long modifiers = type == null ? 0 : type.mods.flags;
		boolean notAClass = (modifiers & (Flags.INTERFACE | Flags.ANNOTATION | Flags.ENUM)) != 0;
		
		if (type == null || notAClass) {
		  annotationNode.addError("@Singleton is only supported on a class.");
		  return false;
		} else {
		  return true;
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
    String holderClassName = annotationNode.getAst().readConfiguration(ConfigurationKeys.SINGLETON_HOLDER_CLASS_NAME);
    if (holderClassName == null) holderClassName = "SingletonHolder";
    if (! holderClassName.isEmpty()) {
      if (! checkName("holderClassName", holderClassName, annotationNode)) return;
    }
    JavacNode holderType = findInnerClass(typeNode, holderClassName);
    if (holderType == null) {
      holderType = makeHolderClass(source, typeNode, holderClassName);
      JCTree tree = annotationNode.get();
      JCVariableDecl holderInstanceField = createField(typeNode, tree, type, "INSTANCE");
      JavacNode heldInstance = injectField(holderType, holderInstanceField);
      JCMethodDecl getterMethodDeclaration = generateGetterMethod(type, staticGetterName, heldInstance.getTreeMaker(), tree, heldInstance);
      injectMethod(typeNode, getterMethodDeclaration);
    } else {
      annotationNode.addWarning("The Singleton holder inner class '" + holderClassName + "' already exists.");
      return;
    }
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
    JavacTreeMaker maker = parent.getTreeMaker();
    int modifiers = Flags.PRIVATE | Flags.STATIC | Flags.FINAL;
    JCModifiers mods = maker.Modifiers(modifiers);
    JCClassDecl holder = maker.ClassDef(mods, parent.toName(holderClassName), copyTypeParams(maker, List.<JCTypeParameter>nil()), null, List.<JCExpression>nil(), List.<JCTree>nil());
    return injectType(parent, holder);
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
  
  /**
   * Checks if there is a nullary constructor.
   * 
   * <p>
   * Also adds a warning if the default constructor is not {@code private}.
   * 
   * @param typeNode  the type node the annotation is declared on
   * @param annotationNode  the annotation node
   */
  private static MemberExistsResult nullaryConstructorExists(JavacNode typeNode, JavacNode annotationNode) {
    typeNode = upToTypeNode(typeNode);
    
    boolean nonNullaryConstructorWarningAdded = false;
    
    if (typeNode != null && typeNode.get() instanceof JCClassDecl) for (JCTree def : ((JCClassDecl)typeNode.get()).defs) {
      if (def instanceof JCMethodDecl) {
        JCMethodDecl md = (JCMethodDecl) def;
        if (md.name.contentEquals("<init>")) {
          if (md.params.size() != 0) {
            if (! nonNullaryConstructorWarningAdded) {
              annotationNode.addWarning("At least one non-nullary constructor exists for this type. Check the need for it.");
              nonNullaryConstructorWarningAdded = true;
            }
            continue;
          }
            
          boolean isDefaultConstrPrivate = ((md.mods.flags & Modifier.PRIVATE) != 0) ? true : false;
          if (! isDefaultConstrPrivate) {
            boolean constrGenerated = true;
            if (constructorExists(typeNode) != MemberExistsResult.NOT_EXISTS) {
              constrGenerated = false;
            }
            new HandleConstructor().generateConstructor(
                typeNode, AccessLevel.PRIVATE, List.<JCAnnotation>nil(), List.<JavacNode>nil(), true, null, SkipIfConstructorExists.YES, false, annotationNode);
            if (! constrGenerated) {
              annotationNode.addWarning("The nullary constructor is not private. Consider marking it private.");
            }
          }
            
          return getGeneratedBy(def) == null ? MemberExistsResult.EXISTS_BY_USER : MemberExistsResult.EXISTS_BY_LOMBOK;
        }
      }
    }
    
    return MemberExistsResult.NOT_EXISTS;
  }
  
}
