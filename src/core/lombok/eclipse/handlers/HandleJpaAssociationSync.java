/*
 * Copyright (C) 2024 The Project Lombok Authors.
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
import static lombok.eclipse.Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import lombok.ConfigurationKeys;
import lombok.core.AST;
import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseASTVisitor;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.experimental.JpaAssociationSync;
import lombok.spi.Provides;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles the {@code lombok.experimental.JpaAssociationSync} annotation for eclipse.
 */
@Provides
public class HandleJpaAssociationSync extends EclipseAnnotationHandler<JpaAssociationSync> {
    private static final String NO_JPA_PACKAGE_IN_CLASSPATH_ERROR = "@JpaAssociationSync requires JPA in classpath";
    private static final String NODE_NOT_SUPPORTED_ERROR = "@JpaAssociationSync is only supported on a class or a field.";

    private static final String ONE_TO_MANY_CLASS_NAME = "OneToMany";
    private static final String MANY_TO_ONE_CLASS_NAME = "ManyToOne";
    private static final String ONE_TO_ONE_CLASS_NAME = "OneToOne";
    private static final String MANY_TO_MANY_CLASS_NAME = "ManyToMany";

    private static final String MAPPED_BY_METHOD_NAME = "mappedBy";

    private static final char[] ADD_METHOD_PREFIX = "add".toCharArray();
    private static final char[] REMOVE_METHOD_PREFIX = "remove".toCharArray();
    private static final char[] UPDATE_METHOD_PREFIX = "update".toCharArray();

    @Override
    public void handle(AnnotationValues<JpaAssociationSync> annotation, Annotation ast, EclipseNode annotationNode) {
        handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.JPA_ASSOCIATION_SYNC_FLAG_USAGE, "@JpaAssociationSync");

        EclipseNode node = annotationNode.up();
        EclipseNode sourceNode = annotationNode;

        String jpaPackageName = getJpaPackageName();

        if (jpaPackageName == null) {
            sourceNode.addError(NO_JPA_PACKAGE_IN_CLASSPATH_ERROR);
            return;
        }

        switch (node.getKind()) {
            case FIELD:
                generateAssociationSyncMethodsForField(node, sourceNode, jpaPackageName);
                break;
            case TYPE:
                generateAssociationSyncMethodsForType(node, sourceNode, jpaPackageName);
                break;
        }
    }

    private void generateAssociationSyncMethodsForType(EclipseNode typeNode,
                                                       final EclipseNode sourceNode,
                                                       final String jpaPackageName) {
        if (!isClass(typeNode)) {
            sourceNode.addError(NODE_NOT_SUPPORTED_ERROR);
            return;
        }

        typeNode.traverse(new EclipseASTVisitor.Default() {
            @Override
            public void visitField(EclipseNode fieldNode, FieldDeclaration field) {
                if (hasAnnotation(JpaAssociationSync.class, fieldNode)) {
                    return;
                }

                generateAssociationSyncMethodsForField(fieldNode, sourceNode, jpaPackageName);
            }
        });
    }

    private void generateAssociationSyncMethodsForField(EclipseNode fieldNode,
                                                        EclipseNode sourceNode,
                                                        String jpaPackageName) {
        if (fieldNode.getKind() != AST.Kind.FIELD) {
            sourceNode.addError(NODE_NOT_SUPPORTED_ERROR);
            return;
        }

        EclipseNode classNode = fieldNode.up();

        Class<? extends java.lang.annotation.Annotation> oneToManyClass
                = findAnnotationClass(jpaPackageName + "." + ONE_TO_MANY_CLASS_NAME);
        Class<? extends java.lang.annotation.Annotation> oneToOneClass
                = findAnnotationClass(jpaPackageName + "." + ONE_TO_ONE_CLASS_NAME);
        Class<? extends java.lang.annotation.Annotation> manyToManyClass
                = findAnnotationClass(jpaPackageName + "." + MANY_TO_MANY_CLASS_NAME);

        List<Class<? extends java.lang.annotation.Annotation>> annotationClasses = new ArrayList<Class<? extends java.lang.annotation.Annotation>>(3);
        annotationClasses.add(oneToManyClass);
        annotationClasses.add(oneToOneClass);
        annotationClasses.add(manyToManyClass);

        List<AssociationDetails> associationDetailsList
                = getAssociationDetailsList(fieldNode, sourceNode, annotationClasses);

        for (AssociationDetails associationDetails : associationDetailsList) {
            if (associationDetails.getType() != null) {
                if (associationDetails.getType().equals(oneToManyClass)) {
                    generateOneToManyAssociationSyncMethodsForField(classNode, fieldNode, sourceNode, associationDetails);
                } else if (associationDetails.getType().equals(oneToOneClass)) {
                    generateOneToOneAssociationSyncMethodsForField(classNode, fieldNode, sourceNode, associationDetails);
                } else if (associationDetails.getType().equals(manyToManyClass)) {
                    generateManyToManyAssociationSyncMethodsForField(classNode, fieldNode, sourceNode, associationDetails);
                }
            }
        }
    }

    private void generateManyToManyAssociationSyncMethodsForField(EclipseNode classNode,
                                                                  EclipseNode fieldNode,
                                                                  EclipseNode sourceNode,
                                                                  AssociationDetails associationDetails) {
        injectMethod(
                classNode,
                createMethodForManyToManyAssociation(classNode, fieldNode, sourceNode, associationDetails, ADD_METHOD_PREFIX)
        );

        injectMethod(
                classNode,
                createMethodForManyToManyAssociation(classNode, fieldNode, sourceNode, associationDetails, REMOVE_METHOD_PREFIX)
        );
    }

    private void generateOneToOneAssociationSyncMethodsForField(EclipseNode classNode,
                                                                EclipseNode fieldNode,
                                                                EclipseNode sourceNode,
                                                                AssociationDetails associationDetails) {
        injectMethod(
                classNode,
                createMethodForOneToOneAssociation(classNode, fieldNode, sourceNode, associationDetails, UPDATE_METHOD_PREFIX)
        );
    }

    private void generateOneToManyAssociationSyncMethodsForField(EclipseNode classNode,
                                                                 EclipseNode fieldNode,
                                                                 EclipseNode sourceNode,
                                                                 AssociationDetails associationDetails) {
        injectMethod(
                classNode,
                createMethodForOneToManyAssociation(classNode, fieldNode, sourceNode, associationDetails, ADD_METHOD_PREFIX)
        );

        injectMethod(
                classNode,
                createMethodForOneToManyAssociation(classNode, fieldNode, sourceNode, associationDetails, REMOVE_METHOD_PREFIX)
        );
    }

    private MethodDeclaration createMethodForManyToManyAssociation(EclipseNode classNode,
                                                                   EclipseNode fieldNode,
                                                                   EclipseNode sourceNode,
                                                                   AssociationDetails associationDetails,
                                                                   char[] methodPrefix) {
        ASTNode source = sourceNode.get();
        FieldDeclaration fieldDecl = (FieldDeclaration) fieldNode.get();
        int pS = source.sourceStart, pE = source.sourceEnd;
        long p = (long) pS << 32 | pE;

        TypeReference paramType = getFieldTypeOrGenericType(fieldDecl);
        char[] paramName = associationDetails.paramName != null
                ? associationDetails.paramName.toCharArray()
                : uncapitalize(sourceNode, String.valueOf(paramType.getLastToken())).toCharArray();
        Argument param = createParameter(paramName, p, paramType, 0);
        param.sourceStart = pS; param.sourceEnd = pE;

        List<Statement> statements = new ArrayList<Statement>();
        statements.add(
                executeMethod(
                        fieldNode,
                        sourceNode,
                        methodPrefix,
                        new Expression[] { new SingleNameReference(param.name, p) }
                )
        );
        statements.add(
                executeMethod(
                        executeMethod(
                                new SingleNameReference(param.name, p),
                                buildAccessorName(
                                        sourceNode,
                                        "get",
                                        associationDetails.inverseSideAnnotation != null
                                                ? associationDetails.owningSideFieldName
                                                : associationDetails.inverseSideFieldName
                                ).toCharArray(),
                                new Expression[] {}
                        ),
                        methodPrefix,
                        new Expression[] { new ThisReference(pS, pE) }
                )
        );

        return createMethod(classNode, sourceNode, param, statements.toArray(new Statement[0]), methodPrefix);
    }

    private MethodDeclaration createMethodForOneToOneAssociation(EclipseNode classNode,
                                                                 EclipseNode fieldNode,
                                                                 EclipseNode sourceNode,
                                                                 AssociationDetails associationDetails,
                                                                 char[] methodPrefix) {
        ASTNode source = sourceNode.get();
        FieldDeclaration fieldDecl = (FieldDeclaration) fieldNode.get();
        int pS = source.sourceStart, pE = source.sourceEnd;
        long p = (long) pS << 32 | pE;

        TypeReference paramType = getFieldTypeOrGenericType(fieldDecl);
        char[] paramName = associationDetails.paramName != null
                ? associationDetails.paramName.toCharArray()
                : uncapitalize(sourceNode, String.valueOf(paramType.getLastToken())).toCharArray();
        Argument param = createParameter(paramName, p, paramType, 0);
        param.sourceStart = pS; param.sourceEnd = pE;

        List<Statement> statements = new ArrayList<Statement>();

            List<Statement> ifStatements = new ArrayList<Statement>();

                List<Statement> if1Statements = new ArrayList<Statement>();;
                if1Statements.add(
                        executeMethod(
                                fieldNode,
                                sourceNode,
                                buildAccessorName(
                                        sourceNode,
                                        "set",
                                        associationDetails.inverseSideAnnotation != null
                                                ? associationDetails.owningSideFieldName
                                                : associationDetails.inverseSideFieldName
                                ).toCharArray(),
                                new Expression[]{ new NullLiteral(pS, pE) }
                        )
                );
                Block if1Block = new Block(0);
                if1Block.statements = if1Statements.toArray(new Statement[0]);

            ifStatements.add(
                    new IfStatement(
                            new EqualExpression(
                                    createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, sourceNode.get()),
                                    new NullLiteral(pS, pE),
                                    Constant.NOT_EQUAL
                            ),
                            if1Block,
                            pS, pE
                    )
            );
            Block ifBlock = new Block(0);
            ifBlock.statements = ifStatements.toArray(new Statement[0]);

            List<Statement> elseStatements = new ArrayList<Statement>();
            elseStatements.add(
                    executeMethod(
                            new SingleNameReference(param.name, p),
                            buildAccessorName(
                                    sourceNode,
                                    "set",
                                    associationDetails.inverseSideAnnotation != null
                                            ? associationDetails.owningSideFieldName
                                            : associationDetails.inverseSideFieldName
                            ).toCharArray(),
                            new Expression[]{ new ThisReference(pS, pE) }
                    )
            );
            Block elseBlock = new Block(0);
            elseBlock.statements = elseStatements.toArray(new Statement[0]);

        statements.add(
                new IfStatement(
                        new EqualExpression(
                                new SingleNameReference(param.name, p),
                                new NullLiteral(pS, pE),
                                Constant.EQUAL_EQUAL
                        ),
                        ifBlock,
                        elseBlock,
                        pS, pE
                )
        );

        statements.add(
                new Assignment(
                        createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, sourceNode.get()),
                        new SingleNameReference(param.name, p),
                        pE
                )
        );

        return createMethod(classNode, sourceNode, param, statements.toArray(new Statement[0]), methodPrefix);
    }

    private MethodDeclaration createMethodForOneToManyAssociation(EclipseNode classNode,
                                                                  EclipseNode fieldNode,
                                                                  EclipseNode sourceNode,
                                                                  AssociationDetails associationDetails,
                                                                  char[] methodPrefix) {
        ASTNode source = sourceNode.get();
        FieldDeclaration fieldDecl = (FieldDeclaration) fieldNode.get();
        int pS = source.sourceStart, pE = source.sourceEnd;
        long p = (long) pS << 32 | pE;

        TypeReference paramType = getFieldTypeOrGenericType(fieldDecl);
        char[] paramName = associationDetails.paramName != null
                ? associationDetails.paramName.toCharArray()
                : uncapitalize(sourceNode, String.valueOf(paramType.getLastToken())).toCharArray();
        Argument param = createParameter(paramName, p, paramType, 0);
        param.sourceStart = pS; param.sourceEnd = pE;

        List<Statement> statements = new ArrayList<Statement>();
        statements.add(
                executeMethod(
                        fieldNode,
                        sourceNode,
                        methodPrefix,
                        new Expression[] { new SingleNameReference(param.name, p) }
                )
        );
        statements.add(
                executeMethod(
                        new SingleNameReference(param.name, p),
                        buildAccessorName(sourceNode, "set", associationDetails.owningSideFieldName).toCharArray(),
                        new Expression[] {
                                Arrays.equals(methodPrefix, ADD_METHOD_PREFIX)
                                        ? new ThisReference(pS, pE)
                                        : new NullLiteral(pS, pE)
                        }
                )
        );

        return createMethod(classNode, sourceNode, param, statements.toArray(new Statement[0]), methodPrefix);
    }

    private MethodDeclaration createMethod(EclipseNode classNode,
                                           EclipseNode sourceNode,
                                           Argument param,
                                           Statement[] statements,
                                           char[] methodPrefix) {
        MethodDeclaration methodDecl = new MethodDeclaration(((TypeDeclaration) classNode.get()).compilationResult);

        ASTNode source = sourceNode.get();
        int pS = source.sourceStart, pE = source.sourceEnd;

        methodDecl.modifiers = Modifier.PUBLIC;
        methodDecl.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
        methodDecl.returnType.sourceStart = pS; methodDecl.returnType.sourceEnd = pE;
        methodDecl.arguments = new Argument[] { param };
        methodDecl.selector = buildAccessorName(sourceNode, String.valueOf(methodPrefix), String.valueOf(param.name)).toCharArray();
        methodDecl.binding = null;
        methodDecl.thrownExceptions = null;
        methodDecl.typeParameters = null;
        methodDecl.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
        methodDecl.statements = statements;

        methodDecl.traverse(new SetGeneratedByVisitor(source), ((TypeDeclaration) classNode.get()).scope);

        return methodDecl;
    }

    private Expression executeMethod(Expression field,
                                     char[] methodName,
                                     Expression[] arguments) {
        MessageSend messageSend = new MessageSend();
        messageSend.receiver = field;
        messageSend.selector = methodName;
        messageSend.arguments = arguments;

        return messageSend;
    }

    private Expression executeMethod(EclipseNode fieldNode,
                                     EclipseNode sourceNode,
                                     char[] methodName,
                                     Expression[] arguments) {
        MessageSend messageSend = new MessageSend();
        messageSend.receiver = createFieldAccessor(fieldNode, FieldAccess.ALWAYS_FIELD, sourceNode.get());
        messageSend.selector = methodName;
        messageSend.arguments = arguments;

        return messageSend;
    }

    private Argument createParameter(char[] paramName,
                                     long pos,
                                     TypeReference paramType,
                                     int modifiers) {
        return new Argument(paramName, pos, paramType, modifiers);
    }

    private TypeReference getFieldTypeOrGenericType(FieldDeclaration fieldDeclaration) {
        TypeReference fieldType = fieldDeclaration.type;

        if (fieldType instanceof ParameterizedQualifiedTypeReference
                || fieldType instanceof ParameterizedSingleTypeReference) {
            return copyType(
                    getGenericTypes(fieldType).get(0)
            );
        }

        return copyType(fieldType);
    }

    private List<AssociationDetails> getAssociationDetailsList(EclipseNode fieldNode,
                                                               EclipseNode sourceNode,
                                                               List<Class<? extends java.lang.annotation.Annotation>> annotationClasses) {
        TypeReference fieldType = getFieldTypeOrGenericType((FieldDeclaration) fieldNode.get());

        AnnotationValues<JpaAssociationSync.Extra> extraSettings = fieldNode.findAnnotation(JpaAssociationSync.Extra.class);

        List<AnnotationValues<? extends java.lang.annotation.Annotation>> associationAnnotations = getAssociationAnnotations(fieldNode, annotationClasses);

        List<AssociationDetails> associationDetailsListBuffer = new ArrayList<AssociationDetails>();
        for (AnnotationValues<? extends java.lang.annotation.Annotation> annotation : associationAnnotations) {
            if (AssociationDetails.isOwningSide(annotation)) {
                if (extraSettings != null
                        && !extraSettings.getAsString("inverseSideFieldName").isEmpty()) {
                    AssociationDetails associationDetails = new AssociationDetails();

                    associationDetails.owningSideAnnotation = annotation;

                    associationDetails.owningSideFieldName = uncapitalize(sourceNode, String.valueOf(fieldType));
                    associationDetails.inverseSideFieldName = extraSettings.getAsString("inverseSideFieldName");

                    associationDetails.paramName =
                            !extraSettings.getAsString("paramName").isEmpty()
                                    ? extraSettings.getAsString("paramName")
                                    : null;

                    associationDetailsListBuffer.add(associationDetails);
                }
            } else {
                AssociationDetails associationDetails = new AssociationDetails();

                associationDetails.inverseSideAnnotation = annotation;

                associationDetails.owningSideFieldName = AssociationDetails.getOwningSideFieldName(annotation);
                associationDetails.inverseSideFieldName = uncapitalize(sourceNode, String.valueOf(fieldType));

                associationDetails.paramName =
                        extraSettings != null && !extraSettings.getAsString("paramName").isEmpty()
                                ? extraSettings.getAsString("paramName")
                                : null;

                associationDetailsListBuffer.add(associationDetails);
            }
        }

        return associationDetailsListBuffer;
    }

    private List<AnnotationValues<? extends java.lang.annotation.Annotation>> getAssociationAnnotations(EclipseNode fieldNode,
                                                                                                        List<Class<? extends java.lang.annotation.Annotation>> annotationClasses) {
        List<AnnotationValues<? extends java.lang.annotation.Annotation>> annotations = new ArrayList<AnnotationValues<? extends java.lang.annotation.Annotation>>();

        for (Class<? extends java.lang.annotation.Annotation> annotationClass : annotationClasses) {
            if (annotationClass != null) {
                AnnotationValues<? extends java.lang.annotation.Annotation> annotation = fieldNode.findAnnotation(annotationClass);

                if (annotation != null) {
                    annotations.add(annotation);
                }
            }
        }

        return annotations;
    }

    private Class<? extends java.lang.annotation.Annotation> findAnnotationClass(String className) {
        try {
            return Class.forName(className).asSubclass(java.lang.annotation.Annotation.class);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private String getJpaPackageName() {
        try {
            Class.forName("javax.persistence.Entity");
            return "javax.persistence";
        } catch (ClassNotFoundException e0) {
            try {
                Class.forName("jakarta.persistence.Entity");
                return "jakarta.persistence";
            } catch (ClassNotFoundException e1) {
                return null;
            }
        }
    }

    private static class AssociationDetails {
        AnnotationValues<? extends java.lang.annotation.Annotation> owningSideAnnotation;
        AnnotationValues<? extends java.lang.annotation.Annotation> inverseSideAnnotation;

        String owningSideFieldName;
        String inverseSideFieldName;

        String paramName;

        AssociationDetails() {
        }

        AssociationDetails(String owningSideFieldName, String inverseSideFieldName) {
            this.owningSideFieldName = owningSideFieldName;
            this.inverseSideFieldName = inverseSideFieldName;
        }

        Class<? extends java.lang.annotation.Annotation> getType() {
            if (owningSideAnnotation != null)
                return owningSideAnnotation.getType();
            if (inverseSideAnnotation != null)
                return inverseSideAnnotation.getType();

            return null;
        }

        static String getOwningSideFieldName(AnnotationValues<? extends java.lang.annotation.Annotation> inverseSideAnnotation) {
            return inverseSideAnnotation.getAsString(MAPPED_BY_METHOD_NAME);
        }

        static boolean isOwningSide(AnnotationValues<? extends java.lang.annotation.Annotation> annotation) {
            return annotation.getAsString(MAPPED_BY_METHOD_NAME).isEmpty();
        }

        static boolean isInverseSide(AnnotationValues<? extends java.lang.annotation.Annotation> annotation) {
            return !isOwningSide(annotation);
        }
    }
}
