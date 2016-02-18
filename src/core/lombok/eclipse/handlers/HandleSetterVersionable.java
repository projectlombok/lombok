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
package lombok.eclipse.handlers;

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.eclipse.Eclipse.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.mangosdk.spi.ProviderFor;

import lombok.AccessLevel;
import lombok.ConfigurationKeys;
import lombok.DataVersionable;
import lombok.SetterVersionable;
import lombok.Version;
import lombok.VersionableUtils;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.FieldAccess;

/**
 * Handles the {@code lombok.SetterVersionable} annotation for eclipse.
 */
@ProviderFor(EclipseAnnotationHandler.class)
public class HandleSetterVersionable extends EclipseAnnotationHandler<SetterVersionable> {
    public boolean generateSetterForType(EclipseNode typeNode, EclipseNode annotationNode, AccessLevel level, boolean checkForTypeLevelSetter, DataVersionable annData) {
        return generateSetterForType(typeNode, annotationNode, level, checkForTypeLevelSetter, null, annData);
    }

    public boolean generateSetterForType(EclipseNode typeNode, EclipseNode pos, AccessLevel level, boolean checkForTypeLevelSetter, SetterVersionable ann, DataVersionable annData) {
        if (checkForTypeLevelSetter) {
            if (hasAnnotation(SetterVersionable.class, typeNode)) {
                //The annotation will make it happen, so we can skip it.
                return true;
            }
        }

        TypeDeclaration typeDecl = null;
        if (typeNode.get() instanceof TypeDeclaration)
            typeDecl = (TypeDeclaration) typeNode.get();
        int modifiers = typeDecl == null ? 0 : typeDecl.modifiers;
        boolean notAClass = (modifiers & (ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) != 0;

        if (typeDecl == null || notAClass) {
            pos.addError("@SetterVersionable is only supported on a class or a field.");
            return false;
        }

        for (EclipseNode field : typeNode.down()) {
            if (field.getKind() != Kind.FIELD)
                continue;
            FieldDeclaration fieldDecl = (FieldDeclaration) field.get();
            if (!filterField(fieldDecl))
                continue;

            //Skip final fields.
            if ((fieldDecl.modifiers & ClassFileConstants.AccFinal) != 0)
                continue;

            if (isValidPrimitive(field, fieldDecl.type)) {
                generateSetterForField(field, pos, level, ann, annData);
            }
        }
        return true;
    }

    /**
     * Generates a setter on the stated field.
     * 
     * Used by {@link HandleData}.
     * 
     * The difference between this call and the handle method is as follows:
     * 
     * If there is a {@code lombok.Setter} annotation on the field, it is used and the
     * same rules apply (e.g. warning if the method already exists, stated access level applies).
     * If not, the setter is still generated if it isn't already there, though there will not
     * be a warning if its already there. The default access level is used.
     * @param ann 
     */
    public void generateSetterForField(EclipseNode fieldNode, EclipseNode sourceNode, AccessLevel level, SetterVersionable ann, DataVersionable annData) {
        if (hasAnnotation(SetterVersionable.class, fieldNode)) {
            //The annotation will make it happen, so we can skip it.
            return;
        }

        List<Annotation> empty = Collections.emptyList();

        createSetterForField(level, fieldNode, sourceNode, false, empty, empty, ann, annData);
    }

    public void handle(AnnotationValues<SetterVersionable> annotation, Annotation ast, EclipseNode annotationNode) {
        handleFlagUsage(annotationNode, ConfigurationKeys.SETTER_VERSIONABLE_FLAG_USAGE, "@SetterVersionable");

        if (isValidPrimitive(annotationNode)) {
            EclipseNode node = annotationNode.up();
            SetterVersionable ann = annotation.getInstance();
            AccessLevel level = ann.value();
            if (level == AccessLevel.NONE || node == null)
                return;

            List<Annotation> onMethod = unboxAndRemoveAnnotationParameter(ast, "onMethod", "@SetterVersionable(onMethod=", annotationNode);
            List<Annotation> onParam = unboxAndRemoveAnnotationParameter(ast, "onParam", "@SetterVersionable(onParam=", annotationNode);

            switch (node.getKind()) {
                case FIELD:
                    createSetterForFields(level, annotationNode.upFromAnnotationToFields(), annotationNode, true, onMethod, onParam, ann, null);
                    break;
                case TYPE:
                    if (!onMethod.isEmpty()) {
                        annotationNode.addError("'onMethod' is not supported for @SetterVersionable on a type.");
                    }
                    if (!onParam.isEmpty()) {
                        annotationNode.addError("'onParam' is not supported for @SetterVersionable on a type.");
                    }
                    generateSetterForType(node, annotationNode, level, false, ann, null);
                    break;
            }
        }
    }

    public void createSetterForFields(AccessLevel level, Collection<EclipseNode> fieldNodes, EclipseNode sourceNode, boolean whineIfExists, List<Annotation> onMethod, List<Annotation> onParam, SetterVersionable ann, DataVersionable annData) {
        for (EclipseNode fieldNode : fieldNodes) {
            createSetterForField(level, fieldNode, sourceNode, whineIfExists, onMethod, onParam, ann, annData);
        }
    }

    public void createSetterForField(AccessLevel level, EclipseNode fieldNode, EclipseNode sourceNode, boolean whineIfExists, List<Annotation> onMethod, List<Annotation> onParam, SetterVersionable ann, DataVersionable annData) {

        ASTNode source = sourceNode.get();
        if (fieldNode.getKind() != Kind.FIELD) {
            sourceNode.addError("@SetterVersionable is only supported on a class or a field.");
            return;
        }

        FieldDeclaration field = (FieldDeclaration) fieldNode.get();
        TypeReference fieldType = copyType(field.type, source);
        boolean isBoolean = isBoolean(fieldType);
        String setterName = toSetterName(fieldNode, isBoolean);
        boolean shouldReturnThis = shouldReturnThis(fieldNode);

        if (setterName == null) {
            fieldNode.addWarning("Not generating setter for this field: It does not fit your @Accessors prefix list.");
            return;
        }

        int modifier = toEclipseModifier(level) | (field.modifiers & ClassFileConstants.AccStatic);

        for (String altName : toAllSetterNames(fieldNode, isBoolean)) {
            switch (methodExists(altName, fieldNode, false, 1)) {
                case EXISTS_BY_LOMBOK:
                    //return;
                case EXISTS_BY_USER:
                    //				if (whineIfExists) {
                    //					String altNameExpl = "";
                    //					if (!altName.equals(setterName)) altNameExpl = String.format(" (%s)", altName);
                    //					fieldNode.addWarning(
                    //						String.format("Not generating %s(): A method with that name already exists%s", setterName, altNameExpl));
                    //				}
                    //				return;
                default:
                case NOT_EXISTS:
                    //continue scanning the other alt names.
            }
        }

        MethodDeclaration method = createSetter((TypeDeclaration) fieldNode.up().get(), fieldNode, setterName, shouldReturnThis, modifier, sourceNode, onMethod, onParam, ann, annData);
        injectMethod(fieldNode.up(), method);
    }

    static MethodDeclaration createSetter(TypeDeclaration parent, EclipseNode fieldNode, String name, boolean shouldReturnThis, int modifier, EclipseNode sourceNode, List<Annotation> onMethod, List<Annotation> onParam, SetterVersionable ann,
            DataVersionable annData) {

        MethodDeclaration method = new MethodDeclaration(parent.compilationResult);
        method.modifiers = modifier;

        FieldDeclaration field = (FieldDeclaration) fieldNode.get();
        ASTNode source = sourceNode.get();
        int pS = source.sourceStart, pE = source.sourceEnd;
        long p = (long) pS << 32 | pE;

        if (shouldReturnThis) {
            method.returnType = cloneSelfType(fieldNode, source);
        }

        if (method.returnType == null) {
            method.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
            method.returnType.sourceStart = pS;
            method.returnType.sourceEnd = pE;
            shouldReturnThis = false;
        }
        Annotation[] deprecated = null;
        if (isFieldDeprecated(fieldNode)) {
            deprecated = new Annotation[] { generateDeprecatedAnnotation(source) };
        }
        method.annotations = copyAnnotations(source, onMethod.toArray(new Annotation[0]), deprecated);
        Argument param = new Argument(field.name, p, copyType(field.type, source), Modifier.FINAL);
        param.sourceStart = pS;
        param.sourceEnd = pE;
        method.arguments = new Argument[] { param };
        method.selector = name.toCharArray();
        method.binding = null;
        method.thrownExceptions = null;
        method.typeParameters = null;
        method.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
        Expression fieldRef = createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, source);
        NameReference fieldNameRef = new SingleNameReference(field.name, p);
        Assignment assignment = new Assignment(fieldRef, fieldNameRef, (int) p);
        assignment.sourceStart = pS;
        assignment.sourceEnd = assignment.statementEnd = pE;
        method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
        method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;

        Annotation[] nonNulls = findAnnotations(field, NON_NULL_PATTERN);
        Annotation[] nullables = findAnnotations(field, NULLABLE_PATTERN);
        List<Statement> statements = new ArrayList<Statement>(5);

        if (nonNulls.length != 0) {
            Statement nullCheck = generateNullCheck(field, sourceNode);
            if (nullCheck != null)
                statements.add(nullCheck);
        }

        IfStatement ifVersionable = generateVersionableIf(p, sourceNode, fieldNode, assignment, ann, annData);

        if (ifVersionable != null) {
            statements.add(ifVersionable);
        }

        if (shouldReturnThis) {
            ThisReference thisRef = new ThisReference(pS, pE);
            ReturnStatement returnThis = new ReturnStatement(thisRef, pS, pE);
            statements.add(returnThis);
        }
        method.statements = statements.toArray(new Statement[0]);
        param.annotations = copyAnnotations(source, nonNulls, nullables, onParam.toArray(new Annotation[0]));

        method.traverse(new SetGeneratedByVisitor(source), parent.scope);

        return method;
    }

    /**
     * if (VersionableUtils.isAllowedForThisVersion(V1,V2,V3...)) {
     *  this.xxx = xxx;
     * } else {
     *  this.xxx = null;
     * }
     * @param ann 
     * @param treeMaker
     * @param field
     * @param source
     * @param assign
     * @return
     */
    private static IfStatement generateVersionableIf(long position, EclipseNode sourceNode, EclipseNode fieldNode, Assignment assignment, SetterVersionable ann, DataVersionable annData) {

        Version versions[] = null;
        if (ann != null) {
            versions = ann.versions();
        } else if (annData != null) {
            versions = annData.versions();
        }

        if (versions != null) {

            // VersionableUtils.isAllowedForThisVersion()
            MessageSend versionableUtilIsAllowedCall = new MessageSend();
            versionableUtilIsAllowedCall.sourceStart = sourceNode.get().sourceStart;
            versionableUtilIsAllowedCall.sourceEnd = sourceNode.get().sourceEnd;
            setGeneratedBy(versionableUtilIsAllowedCall, sourceNode.get());
            versionableUtilIsAllowedCall.receiver = generateQualifiedNameRef(sourceNode.get(), "lombok".toCharArray(), VersionableUtils.class.getSimpleName().toCharArray());
            versionableUtilIsAllowedCall.selector = "isAllowedForThisVersion".toCharArray();

            Expression[] expr = new Expression[versions.length];
            for (int i = 0; i < versions.length; i++) {
                expr[i] = HandleSetterVersionable.generateQualifiedNameRef(sourceNode.get(), "lombok".toCharArray(), Version.class.getSimpleName().toCharArray(), versions[i].name().toCharArray());
            }
            versionableUtilIsAllowedCall.arguments = expr;

            // this.xxx = null
            Expression fieldRef = createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, sourceNode.get());
            NullLiteral nullLiteral = new NullLiteral(sourceNode.get().sourceStart, sourceNode.get().sourceEnd);
            Assignment assignmentNull = new Assignment(fieldRef, nullLiteral, (int) position);

            // this.xxx = xxx
            FieldDeclaration field = (FieldDeclaration) fieldNode.get();
            NameReference fieldNameRef = new SingleNameReference(field.name, position);
            Assignment assignmentThis = new Assignment(fieldRef, fieldNameRef, (int) position);

            return new IfStatement(versionableUtilIsAllowedCall, assignmentThis, assignmentNull, sourceNode.get().sourceStart, sourceNode.get().sourceEnd);
        }
        return null;
    }

    public static NameReference generateQualifiedNameRef(ASTNode source, char[]... varNames) {
        int pS = source.sourceStart, pE = source.sourceEnd;
        long p = (long) pS << 32 | pE;

        NameReference ref;

        if (varNames.length > 1)
            ref = new QualifiedNameReference(varNames, new long[varNames.length], pS, pE);
        else ref = new SingleNameReference(varNames[0], p);
        setGeneratedBy(ref, source);
        return ref;
    }

    private static boolean isValidPrimitive(EclipseNode annotationNode, TypeReference type) {
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

    public static boolean isValidPrimitive(EclipseNode annotationNode) {
        return isValidPrimitive(annotationNode, ((AbstractVariableDeclaration) annotationNode.up().get()).type);
    }
}
