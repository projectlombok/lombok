/*
 * Copyright (C) 2009-2014 The Project Lombok Authors.
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
import static lombok.javac.Javac.*;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import java.util.Collection;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.SetterVersionable;
import lombok.Version;
import lombok.VersionableUtils;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil.CopyJavadoc;
import lombok.javac.handlers.JavacHandlerUtil.FieldAccess;

/**
 * 
 * Handles the {@code lombok.SetterVersionable} annotation for javac.
 *
 * @author https://github.com/lexfaraday
 *
 */
@ProviderFor(JavacAnnotationHandler.class)
public class HandleSetterVersionable extends JavacAnnotationHandler<SetterVersionable> {

    public void generateSetterForType(JavacNode typeNode, JavacNode errorNode, AccessLevel level, boolean checkForTypeLevelSetter, Version[] versions) {
        if (checkForTypeLevelSetter) {
            if (hasAnnotation(SetterVersionable.class, typeNode)) {
                // The annotation will make it happen, so we can skip it.
                return;
            }
        }

        JCClassDecl typeDecl = null;
        if (typeNode.get() instanceof JCClassDecl)
            typeDecl = (JCClassDecl) typeNode.get();
        long modifiers = typeDecl == null ? 0 : typeDecl.mods.flags;
        boolean notAClass = (modifiers & (Flags.INTERFACE | Flags.ANNOTATION | Flags.ENUM)) != 0;

        if (typeDecl == null || notAClass) {
            errorNode.addError("@SetterVersionable is only supported on a class or a field.");
            return;
        }

        for (JavacNode field : typeNode.down()) {
            if (field.getKind() != Kind.FIELD)
                continue;
            JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
            // Skip fields that start with $
            if (fieldDecl.name.toString().startsWith("$"))
                continue;
            // Skip static fields.
            if ((fieldDecl.mods.flags & Flags.STATIC) != 0)
                continue;
            // Skip final fields.
            if ((fieldDecl.mods.flags & Flags.FINAL) != 0)
                continue;

            if (isValidPrimitive(field, fieldDecl.vartype)) {
                generateSetterForField(field, errorNode, level, versions);
            }
        }
    }

    /**
     * Generates a setter on the stated field.
     * 
     * Used by {@link HandleData}.
     * 
     * The difference between this call and the handle method is as follows:
     * 
     * If there is a {@code lombok.Setter} annotation on the field, it is used
     * and the same rules apply (e.g. warning if the method already exists,
     * stated access level applies). If not, the setter is still generated if it
     * isn't already there, though there will not be a warning if its already
     * there. The default access level is used.
     * 
     * @param fieldNode
     *            The node representing the field you want a setter for.
     * @param ann 
     * @param pos
     *            The node responsible for generating the setter (the
     *            {@code @Data} or {@code @SetterVersionable} annotation).
     */
    public void generateSetterForField(JavacNode fieldNode, JavacNode sourceNode, AccessLevel level, Version[] versions) {
        if (hasAnnotation(SetterVersionable.class, fieldNode)) {
            // The annotation will make it happen, so we can skip it.
            return;
        }

        createSetterForField(level, fieldNode, sourceNode, false, List.<JCAnnotation> nil(), List.<JCAnnotation> nil(), versions);
    }

    @Override
    public void handle(AnnotationValues<SetterVersionable> annotation, JCAnnotation ast, JavacNode annotationNode) {
        handleFlagUsage(annotationNode, ConfigurationKeys.SETTER_VERSIONABLE_FLAG_USAGE, "@SetterVersionable");

        if (isValidPrimitive(annotationNode)) {
            Collection<JavacNode> fields = annotationNode.upFromAnnotationToFields();
            deleteAnnotationIfNeccessary(annotationNode, SetterVersionable.class);
            deleteImportFromCompilationUnit(annotationNode, "lombok.AccessLevel");

            JavacNode node = annotationNode.up();
            // Get current annotation
            Version versions[] = annotation.getInstance().versions();
            AccessLevel level = annotation.getInstance().value();

            if (level == AccessLevel.NONE || node == null)
                return;

            List<JCAnnotation> onMethod = unboxAndRemoveAnnotationParameter(ast, "onMethod", "@SetterVersionable(onMethod=", annotationNode);
            List<JCAnnotation> onParam = unboxAndRemoveAnnotationParameter(ast, "onParam", "@SetterVersionable(onParam=", annotationNode);

            switch (node.getKind()) {
                case FIELD:
                    createSetterForFields(level, fields, annotationNode, true, onMethod, onParam, versions);
                    break;
                case TYPE:
                    if (!onMethod.isEmpty())
                        annotationNode.addError("'onMethod' is not supported for @SetterVersionable on a type.");
                    if (!onParam.isEmpty())
                        annotationNode.addError("'onParam' is not supported for @SetterVersionable on a type.");
                    generateSetterForType(node, annotationNode, level, false, versions);
                    break;
            }
        }
    }

    public void createSetterForFields(AccessLevel level, Collection<JavacNode> fieldNodes, JavacNode errorNode, boolean whineIfExists, List<JCAnnotation> onMethod, List<JCAnnotation> onParam, Version[] versions) {
        for (JavacNode fieldNode : fieldNodes) {
            createSetterForField(level, fieldNode, errorNode, whineIfExists, onMethod, onParam, versions);
        }
    }

    public void createSetterForField(AccessLevel level, JavacNode fieldNode, JavacNode sourceNode, boolean whineIfExists, List<JCAnnotation> onMethod, List<JCAnnotation> onParam, Version[] versions) {
        if (fieldNode.getKind() != Kind.FIELD) {
            fieldNode.addError("@SetterVersionable is only supported on a class or a field.");
            return;
        }

        JCVariableDecl fieldDecl = (JCVariableDecl) fieldNode.get();
        String methodName = toSetterName(fieldNode);

        if (methodName == null) {
            fieldNode.addWarning("Not generating setter for this field: It does not fit your @Accessors prefix list.");
            return;
        }

        if ((fieldDecl.mods.flags & Flags.FINAL) != 0) {
            fieldNode.addWarning("Not generating setter for this field: Setters cannot be generated for final fields.");
            return;
        }

        for (String altName : toAllSetterNames(fieldNode)) {
            switch (methodExists(altName, fieldNode, false, 1)) {
                case EXISTS_BY_LOMBOK:
                    // return;
                case EXISTS_BY_USER:
                    // if (whineIfExists) {
                    // String altNameExpl = "";
                    // if (!altName.equals(methodName)) altNameExpl =
                    // String.format(" (%s)", altName);
                    // fieldNode.addWarning(
                    // String.format("Not generating %s(): A method with that name
                    // already exists%s", methodName, altNameExpl));
                    // }
                    // return;
                default:
                case NOT_EXISTS:
                    // continue scanning the other alt names.
            }
        }

        long access = toJavacModifier(level) | (fieldDecl.mods.flags & Flags.STATIC);

        JCMethodDecl createdSetter = createSetter(access, fieldNode, fieldNode.getTreeMaker(), sourceNode, onMethod, onParam, versions);
        injectMethod(fieldNode.up(), createdSetter);
    }

    public static JCMethodDecl createSetter(long access, JavacNode field, JavacTreeMaker treeMaker, JavacNode source, List<JCAnnotation> onMethod, List<JCAnnotation> onParam, Version[] versions) {
        String setterName = toSetterName(field);
        boolean returnThis = shouldReturnThis(field);
        return createSetter(access, field, treeMaker, setterName, returnThis, source, onMethod, onParam, versions);
    }

    public static JCMethodDecl createSetter(long access, JavacNode field, JavacTreeMaker treeMaker, String setterName, boolean shouldReturnThis, JavacNode source, List<JCAnnotation> onMethod, List<JCAnnotation> onParam, Version[] versions) {
        if (setterName == null)
            return null;

        JCVariableDecl fieldDecl = (JCVariableDecl) field.get();

        JCExpression fieldRef = createFieldAccessor(treeMaker, field, FieldAccess.ALWAYS_FIELD);
        JCAssign assign = treeMaker.Assign(fieldRef, treeMaker.Ident(fieldDecl.name));

        ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
        List<JCAnnotation> nonNulls = findAnnotations(field, NON_NULL_PATTERN);
        List<JCAnnotation> nullables = findAnnotations(field, NULLABLE_PATTERN);

        Name methodName = field.toName(setterName);
        List<JCAnnotation> annsOnParam = copyAnnotations(onParam).appendList(nonNulls).appendList(nullables);

        long flags = JavacHandlerUtil.addFinalIfNeeded(Flags.PARAMETER, field.getContext());
        JCVariableDecl param = treeMaker.VarDef(treeMaker.Modifiers(flags, annsOnParam), fieldDecl.name, fieldDecl.vartype, null);

        if (!nonNulls.isEmpty()) {
            JCStatement nullCheck = generateNullCheck(treeMaker, field, source);
            if (nullCheck != null)
                statements.append(nullCheck);
        }

        // Add setter with Version
        JCStatement ifVersionable = HandleSetterVersionable.generateVersionableIf(treeMaker, field, source, assign, versions);
        if (ifVersionable != null) {
            statements.append(ifVersionable);
        }

        JCExpression methodType = null;
        if (shouldReturnThis) {
            methodType = cloneSelfType(field);
        }

        if (methodType == null) {
            // WARNING: Do not use field.getSymbolTable().voidType - that field
            // has gone through non-backwards compatible API changes within
            // javac1.6.
            methodType = treeMaker.Type(Javac.createVoidType(treeMaker, CTC_VOID));
            shouldReturnThis = false;
        }

        if (shouldReturnThis) {
            JCReturn returnStatement = treeMaker.Return(treeMaker.Ident(field.toName("this")));
            statements.append(returnStatement);
        }

        JCBlock methodBody = treeMaker.Block(0, statements.toList());
        List<JCTypeParameter> methodGenericParams = List.nil();
        List<JCVariableDecl> parameters = List.of(param);
        List<JCExpression> throwsClauses = List.nil();
        JCExpression annotationMethodDefaultValue = null;

        List<JCAnnotation> annsOnMethod = copyAnnotations(onMethod);
        if (isFieldDeprecated(field)) {
            annsOnMethod = annsOnMethod.prepend(treeMaker.Annotation(genJavaLangTypeRef(field, "Deprecated"), List.<JCExpression> nil()));
        }

        JCMethodDecl decl = recursiveSetGeneratedBy(
                                                    treeMaker.MethodDef(treeMaker.Modifiers(access, annsOnMethod), methodName, methodType, methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue),
                                                        source.get(),
                                                        field.getContext());
        copyJavadoc(field, decl, CopyJavadoc.SETTER);

        return decl;
    }

    /**
     * if (VersionableUtils.isAllowedForThisVersion(V1,V2,V3...)) {
     *  this.xxx = xxx;
     * } else {
     *  this.xxx = null;
     * }
     * @param treeMaker
     * @param field
     * @param source
     * @param assign
     * @param ann 
     * @return
     */
    private static JCStatement generateVersionableIf(JavacTreeMaker treeMaker, JavacNode field, JavacNode source, JCAssign assign, Version[] versions) {
        if (versions != null) {
            ListBuffer<JCExpression> versionExpressions = new ListBuffer<JCExpression>();
            for (int i = versions.length - 1; i >= 0; i--) {
                Version version = versions[i];
                versionExpressions.add(JavacHandlerUtil.chainDots(field, "lombok", Version.class.getSimpleName(), version.toString()));
            }

            // VersionableUtils.isAllowedForThisVersion()
            JCExpression versionableUtils = JavacHandlerUtil.chainDots(field, "lombok", VersionableUtils.class.getSimpleName());
            JCExpression isAllowedForThisVersion = treeMaker.Apply(List.<JCExpression> nil(), treeMaker.Select(versionableUtils, field.toName("isAllowedForThisVersion")), versionExpressions.toList());
            // this.xxx = null
            JCExpression fieldRef = createFieldAccessor(treeMaker, field, FieldAccess.ALWAYS_FIELD);
            JCAssign assignNull = treeMaker.Assign(fieldRef, treeMaker.Literal(CTC_BOT, null));
            return treeMaker.If(isAllowedForThisVersion, treeMaker.Exec(assign), treeMaker.Exec(assignNull));

        }
        return null;
    }

    /**
     * TODO why duplicate method between eclipse and javac? :S
     * @param annotationNode
     * @param type
     * @return
     */
    private static boolean isValidPrimitive(JavacNode annotationNode, JCExpression type) {
        if (annotationNode.up().getKind() == Kind.FIELD || annotationNode.getKind() == Kind.FIELD) {
            try {
                if (isPrimitive(type)) {
                    annotationNode.addError("For @SetterVersionable or @DataVersionable primitive types are not allowed, please use the wrapper types");
                    return false;
                }
            } catch (Exception ignore) {
            }
        }
        return true;
    }

    private static boolean isValidPrimitive(JavacNode annotationNode) {
        return isValidPrimitive(annotationNode, ((JCVariableDecl) annotationNode.up().get()).vartype);
    }

}
