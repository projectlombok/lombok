/*
 * Copyright (C) 2012-2014 The Project Lombok Authors.
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

import static lombok.eclipse.Eclipse.*;
import static lombok.core.handlers.HandlerUtil.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.core.AST.Kind;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.MemberExistsResult;
import lombok.experimental.Singleton;
import lombok.experimental.Tolerate;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.mangosdk.spi.ProviderFor;

/**
 * Handles the {@code lombok.Singleton} annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleSingleton extends EclipseAnnotationHandler<Singleton> {
  @Override
	public void handle(AnnotationValues<Singleton> annotation, Annotation ast, EclipseNode annotationNode) {
		handleFlagUsage(annotationNode, ConfigurationKeys.VALUE_FLAG_USAGE, "@Singleton");
		
		EclipseNode typeNode = annotationNode.up();
		
		TypeDeclaration type = null;
		if (typeNode.get() instanceof TypeDeclaration) type = (TypeDeclaration) typeNode.get();
		boolean isUsedOnClass = isUsedOnClass(type, annotationNode);
		if (! isUsedOnClass) return;
		
		if (nonDefaultConstructorExists(typeNode) != MemberExistsResult.NOT_EXISTS) {
      annotationNode.addWarning("A non-default constructor exists. Consider removing it for the default constructor." + nonDefaultConstructorExists(typeNode));
    }
		
		if (defaultConstructorExists(typeNode, annotationNode) == MemberExistsResult.NOT_EXISTS) {
      annotationNode.addError("A default constructor does not exist. Consider removing any non-default constructor for the default one.");
      return;
    }
		
		String staticGetterName = annotationNode.getAst().readConfiguration(ConfigurationKeys.SINGLETON_STATIC_GETTER_NAME);
		if (staticGetterName == null) staticGetterName = "getInstance";
		boolean getterExists = getterExists(typeNode, annotationNode, staticGetterName);
		if (getterExists) return;
		
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
      return;
    }
	}

  /**
   * Checks if a getter method with the given name already exists.
   * 
   * <p>
   * Also adds an error to the annotation node if it exists already.
   * 
   * @param typeNode  the type the annotation is used on
   * @param annotationNode  the annotation node
   * @param staticGetterName  the name of the getter method
   * @return {@code true} if a getter method with the given name: <tt>staticGetterName</tt>
   * already exists; {@code false} otherwise
   */
  private boolean getterExists(EclipseNode typeNode, EclipseNode annotationNode, String staticGetterName) {
    MemberExistsResult getterExistsResult = methodExists(staticGetterName, typeNode, true, 0);
		if (getterExistsResult != MemberExistsResult.NOT_EXISTS) {
		  annotationNode.addWarning("The static instance getter method: '" + staticGetterName + "' already exists.");
		  return true;
		} else {
		  return false;
		}
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
  private boolean isUsedOnClass(TypeDeclaration type, EclipseNode annotationNode) {
    int modifiers = type == null ? 0 : type.modifiers;
		boolean notAClass = (modifiers &
				(ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) != 0;
		
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
  private void handleEagerInstantiation(Annotation source, EclipseNode typeNode, EclipseNode annotationNode,
      TypeDeclaration type, String staticGetterName) {
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
    
    FieldDeclaration fieldDeclaration = createField(source, instanceFieldName, typeNode);
    fieldDeclaration.traverse(new SetGeneratedByVisitor(source), type.staticInitializerScope);
    // TODO temporary workaround for issue 217. http://code.google.com/p/projectlombok/issues/detail?id=217
    // injectFieldSuppressWarnings(owner, fieldDeclaration);
    EclipseNode instanceField = injectField(typeNode, fieldDeclaration);
    
    MethodDeclaration md = generateGetterMethod(staticGetterName, typeNode, type.typeParameters, source, null, instanceField);
    injectMethod(typeNode, md);
    
    typeNode.rebuild();
  }
	
	/**
	 * Creates the instance field with the given name by calling the nullary constructor.
	 * 
	 * @param source  the {@code AST} setup
	 * @param instanceName  the instance field name
	 * @param typeNode  the enclosing type node
	 * @return  the created field
	 */
	private static FieldDeclaration createField(Annotation source, String instanceName, EclipseNode typeNode) {
    int pS = source.sourceStart, pE = source.sourceEnd;
    long p = (long)pS << 32 | pE;
    
    FieldDeclaration fieldDecl = new FieldDeclaration(instanceName.toCharArray(), 0, -1);
    setGeneratedBy(fieldDecl, source);
    fieldDecl.declarationSourceEnd = -1;
    fieldDecl.modifiers = Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL;
    fieldDecl.type = createTypeReference(typeNode.getName(), source);
    
    AllocationExpression init = new AllocationExpression();
    /*init.sourceStart = fieldDecl.initialization.sourceStart; fieldDecl.sourceEnd = init.statementEnd = fieldDecl.initialization.sourceEnd;*/
    init.type = new SingleTypeReference(typeNode.getName().toCharArray(), 0L);
    
    fieldDecl.initialization = init;
    
    return fieldDecl;
  }

	/**
   * Handles a request to lazily instantiate the Singleton class by creating
   * a static inner Holder class to hold the Singleton instance.
   * 
   * @param source  the {@code AST} setup
   * @param typeNode  the declaring type
   * @param annotationNode  the annotation node to declare warnings on
   * @param type  the enclosing type declaration
   * @param staticGetterName  the getter of the instance
   */
  private void handleLazyInstantiation(Annotation source, EclipseNode typeNode, EclipseNode annotationNode,
      TypeDeclaration type, String staticGetterName) {
    String holderClassName = annotationNode.getAst().readConfiguration(ConfigurationKeys.SINGLETON_HOLDER_CLASS_NAME);
		if (holderClassName == null) holderClassName = "SingletonHolder";
		if (!holderClassName.isEmpty()) {
      if (!checkName("holderClassName", holderClassName, annotationNode)) return;
    }
		
		EclipseNode holderType = findInnerClass(typeNode, holderClassName);
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
    }
  }
	
	/**
	 * Finds an inner class with the given name.
	 * 
	 * @param parent  the enclosing node
	 * @param name  the name of the inner class
	 * @return  the inner class with the given name; {@code null}
	 * if none was found
	 */
	private EclipseNode findInnerClass(EclipseNode parent, String name) {
    char[] c = name.toCharArray();
    for (EclipseNode child : parent.down()) {
      if (child.getKind() != Kind.TYPE) continue;
      TypeDeclaration td = (TypeDeclaration) child.get();
      if (Arrays.equals(td.name, c)) return child;
    }
    return null;
  }
	
	/**
   * Finds a field with the given name.
   * 
   * @param parent  the enclosing node
   * @param name  the name of the field
   * @return  the inner class with the given field; {@code null}
   * if none was found
   */
	private EclipseNode findField(EclipseNode parent, String name) {
    char[] c = name.toCharArray();
    for (EclipseNode child : parent.down()) {
      if (child.getKind() != Kind.FIELD) continue;
      FieldDeclaration td = (FieldDeclaration) child.get();
      if (Arrays.equals(td.name, c)) return child;
    }
    return null;
  }
	
	/**
	 * Creates a type reference given the type name.
	 * 
	 * @param typeName  the reference type name
	 * @param source  the {@code AST} setup
	 * @return a type reference given the type name
	 */
	private static TypeReference createTypeReference(String typeName, Annotation source) {
    int pS = source.sourceStart, pE = source.sourceEnd;
    long p = (long)pS << 32 | pE;
    
    TypeReference typeReference = new SingleTypeReference(typeName.toCharArray(), 0L);
    
    setGeneratedBy(typeReference, source);
    return typeReference;
  }
	
	private MethodDeclaration generateGetterMethod(String staticGetterName, EclipseNode type, TypeParameter[] typeParams, ASTNode source,
	    String holderClassName, EclipseNode heldInstance) {
    int pS = source.sourceStart, pE = source.sourceEnd;
    long p = (long) pS << 32 | pE;
    
    MethodDeclaration out = new MethodDeclaration(((CompilationUnitDeclaration) type.top().get()).compilationResult);
    out.selector = staticGetterName.toCharArray();
    out.modifiers = ClassFileConstants.AccPublic;
    out.modifiers |= ClassFileConstants.AccStatic;
    out.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
    out.returnType = namePlusTypeParamsToTypeReference(type.getName().toCharArray(), typeParams, p);
    out.typeParameters = copyTypeParams(typeParams, source);
    
    FieldDeclaration fieldDecl = (FieldDeclaration)heldInstance.get();
    FieldReference ref = new FieldReference(fieldDecl.name, p);
    EclipseNode containerNode = heldInstance.up();
    ref.receiver = new SingleNameReference(((TypeDeclaration) containerNode.get()).name, p);
    setGeneratedBy(ref, source);
    setGeneratedBy(ref.receiver, source);
    out.statements = new Statement[] {new ReturnStatement(ref, ref.sourceStart, ref.sourceEnd)};
    out.traverse(new SetGeneratedByVisitor(source), ((TypeDeclaration) type.get()).scope);
    
    return out;
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
	private EclipseNode makeHolderClass(Annotation ast, EclipseNode parent, String holderClassName) {
    TypeDeclaration parentTypeDecl = (TypeDeclaration) parent.get();
    TypeDeclaration holder = new TypeDeclaration(parentTypeDecl.compilationResult);
    holder.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
    holder.modifiers |= ClassFileConstants.AccPublic;
    holder.modifiers |= ClassFileConstants.AccStatic;
    holder.modifiers |= ClassFileConstants.AccFinal;
    holder.name = holderClassName.toCharArray();
    holder.traverse(new SetGeneratedByVisitor(ast), (ClassScope) null);
    
    return injectType(parent, holder);
  }
	
	/**
   * Checks if there is a default constructor.
   * 
   * <p>
   * Also adds a warning if the default constructor is not {@code private}.
   * 
   * @param typeNode  the type node the annotation is declared on
   * @param annotationNode  the annotation node
   */
  private static MemberExistsResult defaultConstructorExists(EclipseNode typeNode, EclipseNode annotationNode) {
    while (typeNode != null && !(typeNode.get() instanceof TypeDeclaration)) {
      typeNode = typeNode.up();
    }
    
    if (typeNode != null && typeNode.get() instanceof TypeDeclaration) {
      TypeDeclaration typeDecl = (TypeDeclaration)typeNode.get();
      if (typeDecl.methods != null) for (AbstractMethodDeclaration def : typeDecl.methods) {
        if (def instanceof ConstructorDeclaration) {
          if ((def.bits & ASTNode.IsDefaultConstructor) == 0) continue;
          
          boolean isDefaultConstrPrivate = ((typeDecl.modifiers & Modifier.PRIVATE) != 0) ? true : false;
          if (! isDefaultConstrPrivate) {
            annotationNode.addWarning("The default constructor is not private! Considering marking it private.");
          }
          return getGeneratedBy(def) == null ? MemberExistsResult.EXISTS_BY_USER : MemberExistsResult.EXISTS_BY_LOMBOK;
        }
      }
    }
    
    return MemberExistsResult.NOT_EXISTS;
  }
  
  /**
   * Checks if there is a non-default constructor.
   * 
   * @param typeNode  the type node the annotation is declared on
   */
  private static MemberExistsResult nonDefaultConstructorExists(EclipseNode typeNode) {
    while (typeNode != null && !(typeNode.get() instanceof TypeDeclaration)) {
      typeNode = typeNode.up();
    }
    
    if (typeNode != null && typeNode.get() instanceof TypeDeclaration) {
      TypeDeclaration typeDecl = (TypeDeclaration)typeNode.get();
      if (typeDecl.methods != null) for (AbstractMethodDeclaration def : typeDecl.methods) {
        if (def instanceof ConstructorDeclaration) {
          System.out
              .println("Checking: " + def);
          if ((def.bits & ASTNode.IsDefaultConstructor) != 0) continue;
          
          return getGeneratedBy(def) == null ? MemberExistsResult.EXISTS_BY_USER : MemberExistsResult.EXISTS_BY_LOMBOK;
        }
      }
    }
    
    return MemberExistsResult.NOT_EXISTS;
  }
}
