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
package lombok.javac.handlers;

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.javac.Javac.*;
import static lombok.javac.JavacAugments.JCTree_keepPosition;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import lombok.ConfigurationKeys;
import lombok.core.AST;
import lombok.core.AnnotationValues;
import lombok.experimental.JpaAssociationSync;
import lombok.javac.JavacASTVisitor;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.spi.Provides;

import java.lang.annotation.Annotation;

/**
 * Handles the {@code lombok.experimental.JpaAssociationSync} annotation for javac.
 */
@Provides
public class HandleJpaAssociationSync extends JavacAnnotationHandler<JpaAssociationSync> {
    private static final String NO_JPA_PACKAGE_IN_CLASSPATH_ERROR = "@JpaAssociationSync requires JPA in classpath";
    private static final String NODE_NOT_SUPPORTED_ERROR = "@JpaAssociationSync is only supported on a class or a field.";

    private static final String ONE_TO_MANY_CLASS_NAME = "OneToMany";
    private static final String MANY_TO_ONE_CLASS_NAME = "ManyToOne";
    private static final String ONE_TO_ONE_CLASS_NAME = "OneToOne";
    private static final String MANY_TO_MANY_CLASS_NAME = "ManyToMany";

    private static final String MAPPED_BY_METHOD_NAME = "mappedBy";

    private static final String ADD_METHOD_PREFIX = "add";
    private static final String REMOVE_METHOD_PREFIX = "remove";
    private static final String UPDATE_METHOD_PREFIX = "update";

    @Override
    public void handle(AnnotationValues<JpaAssociationSync> annotation, JCAnnotation ast, JavacNode annotationNode) {
        handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.JPA_ASSOCIATION_SYNC_FLAG_USAGE, "@JpaAssociationSync");
        deleteAnnotationIfNeccessary(annotationNode, JpaAssociationSync.class);

        JavacNode node = annotationNode.up();
        JavacNode sourceNode = annotationNode;
        JavacTreeMaker treeMaker = annotationNode.getTreeMaker();

        String jpaPackageName = getJpaPackageName();

        if (jpaPackageName == null) {
            sourceNode.addError(NO_JPA_PACKAGE_IN_CLASSPATH_ERROR);
            return;
        }

        switch (node.getKind()) {
            case FIELD:
                generateAssociationSyncMethodsForField(node, sourceNode, treeMaker, jpaPackageName);
                break;
            case TYPE:
                generateAssociationSyncMethodsForType(node, sourceNode, treeMaker, jpaPackageName);
                break;
        }
    }

    private void generateAssociationSyncMethodsForType(JavacNode typeNode,
                                                       final JavacNode sourceNode,
                                                       final JavacTreeMaker treeMaker,
                                                       final String jpaPackageName) {
        if (!isClass(typeNode)) {
            sourceNode.addError(NODE_NOT_SUPPORTED_ERROR);
            return;
        }

        typeNode.traverse(new JavacASTVisitor.Default() {
            @Override
            public void visitField(JavacNode fieldNode, JCVariableDecl field) {
                if (hasAnnotation(JpaAssociationSync.class, fieldNode)) {
                    return;
                }

                generateAssociationSyncMethodsForField(fieldNode, sourceNode, treeMaker, jpaPackageName);
            }
        });
    }

    private void generateAssociationSyncMethodsForField(JavacNode fieldNode,
                                                        JavacNode sourceNode,
                                                        JavacTreeMaker treeMaker,
                                                        String jpaPackageName) {
        if (fieldNode.getKind() != AST.Kind.FIELD) {
            sourceNode.addError(NODE_NOT_SUPPORTED_ERROR);
            return;
        }

        JavacNode classNode = fieldNode.up();

        Class<? extends Annotation> oneToManyClass
                = findAnnotationClass(jpaPackageName + "." + ONE_TO_MANY_CLASS_NAME);
        Class<? extends Annotation> oneToOneClass
                = findAnnotationClass(jpaPackageName + "." + ONE_TO_ONE_CLASS_NAME);
        Class<? extends Annotation> manyToManyClass
                = findAnnotationClass(jpaPackageName + "." + MANY_TO_MANY_CLASS_NAME);

        List<Class<? extends Annotation>> annotationClasses = List.of(oneToManyClass, oneToOneClass, manyToManyClass);

        List<AssociationDetails> associationDetailsList
                = getAssociationDetailsList(fieldNode, sourceNode, treeMaker, annotationClasses);

        for (AssociationDetails associationDetails : associationDetailsList) {
            if (associationDetails.getType() != null) {
                if (associationDetails.getType().equals(oneToManyClass)) {
                    generateOneToManyAssociationSyncMethodsForField(classNode, fieldNode, sourceNode, treeMaker, associationDetails);
                } else if (associationDetails.getType().equals(oneToOneClass)) {
                    generateOneToOneAssociationSyncMethodsForField(classNode, fieldNode, sourceNode, treeMaker, associationDetails);
                } else if (associationDetails.getType().equals(manyToManyClass)) {
                    generateManyToManyAssociationSyncMethodsForField(classNode, fieldNode, sourceNode, treeMaker, associationDetails);
                }
            }
        }
    }

    private void generateManyToManyAssociationSyncMethodsForField(JavacNode classNode,
                                                                  JavacNode fieldNode,
                                                                  JavacNode sourceNode,
                                                                  JavacTreeMaker treeMaker,
                                                                  AssociationDetails associationDetails) {
        injectMethod(
                classNode,
                createMethodForManyToManyAssociation(fieldNode, sourceNode, treeMaker, associationDetails, ADD_METHOD_PREFIX)
        );

        injectMethod(
                classNode,
                createMethodForManyToManyAssociation(fieldNode, sourceNode, treeMaker, associationDetails, REMOVE_METHOD_PREFIX)
        );
    }

    private void generateOneToOneAssociationSyncMethodsForField(JavacNode classNode,
                                                                JavacNode fieldNode,
                                                                JavacNode sourceNode,
                                                                JavacTreeMaker treeMaker,
                                                                AssociationDetails associationDetails) {
        injectMethod(
                classNode,
                createMethodForOneToOneAssociation(fieldNode, sourceNode, treeMaker, associationDetails, UPDATE_METHOD_PREFIX)
        );
    }

    private void generateOneToManyAssociationSyncMethodsForField(JavacNode classNode,
                                                                 JavacNode fieldNode,
                                                                 JavacNode sourceNode,
                                                                 JavacTreeMaker treeMaker,
                                                                 AssociationDetails associationDetails) {
        injectMethod(
                classNode,
                createMethodForOneToManyAssociation(fieldNode, sourceNode, treeMaker, associationDetails, ADD_METHOD_PREFIX)
        );

        injectMethod(
                classNode,
                createMethodForOneToManyAssociation(fieldNode, sourceNode, treeMaker, associationDetails, REMOVE_METHOD_PREFIX)
        );
    }

    private JCMethodDecl createMethodForManyToManyAssociation(JavacNode fieldNode,
                                                              JavacNode sourceNode,
                                                              JavacTreeMaker treeMaker,
                                                              AssociationDetails associationDetails,
                                                              String methodPrefix) {
        JCVariableDecl fieldDecl = (JCVariableDecl) fieldNode.get();

        JCIdent paramType = getFieldTypeOrGenericType(fieldDecl, sourceNode, treeMaker);
        Name paramName = associationDetails.paramName != null
                ? sourceNode.toName(associationDetails.paramName)
                : sourceNode.toName(uncapitalize(sourceNode, paramType.getName().toString()));
        JCVariableDecl param = createParameter(treeMaker, paramName, paramType);

        ListBuffer<JCVariableDecl> parameters = new ListBuffer<JCVariableDecl>();
        parameters.append(param);

        ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
        statements.append(
                executeMethod(
                        treeMaker,
                        createFieldAccessor(treeMaker, fieldNode, FieldAccess.ALWAYS_FIELD),
                        sourceNode.toName(methodPrefix),
                        List.<JCExpression>of(
                                treeMaker.Ident(param.name)
                        )
                )
        );
        statements.append(
                executeMethod(
                        treeMaker,
                        treeMaker.Apply(
                                List.<JCExpression>nil(),
                                treeMaker.Select(
                                        treeMaker.Ident(param.name),
                                        sourceNode.toName(
                                                buildAccessorName(
                                                        sourceNode,
                                                        "get",
                                                        associationDetails.inverseSideAnnotation != null
                                                                ? associationDetails.owningSideFieldName
                                                                : associationDetails.inverseSideFieldName
                                                )
                                        )
                                ),
                                List.<JCExpression>nil()
                        ),
                        sourceNode.toName(methodPrefix),
                        List.<JCExpression>of(
                                treeMaker.Ident(sourceNode.toName("this"))
                        )
                )
        );

        JCBlock methodBody = treeMaker.Block(0, statements.toList());

        JCMethodDecl methodDecl = treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                sourceNode.toName(methodPrefix + capitalize(sourceNode, param.name.toString())),
                treeMaker.Type(createVoidType(sourceNode.getSymbolTable(), CTC_VOID)),
                List.<JCTypeParameter>nil(),
                parameters.toList(),
                List.<JCExpression>nil(),
                methodBody,
                null
        );

        return recursiveSetGeneratedBy(methodDecl, sourceNode);
    }

    private JCMethodDecl createMethodForOneToOneAssociation(JavacNode fieldNode,
                                                            JavacNode sourceNode,
                                                            JavacTreeMaker treeMaker,
                                                            AssociationDetails associationDetails,
                                                            String methodPrefix) {
        JCVariableDecl fieldDecl = (JCVariableDecl) fieldNode.get();

        JCIdent paramType = getFieldTypeOrGenericType(fieldDecl, sourceNode, treeMaker);
        Name paramName = associationDetails.paramName != null
                ? sourceNode.toName(associationDetails.paramName)
                : sourceNode.toName(uncapitalize(sourceNode, paramType.getName().toString()));
        JCVariableDecl param = createParameter(treeMaker, paramName, paramType);

        ListBuffer<JCVariableDecl> parameters = new ListBuffer<JCVariableDecl>();
        parameters.append(param);

        ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();

            ListBuffer<JCStatement> ifStatements = new ListBuffer<JCStatement>();

                ListBuffer<JCStatement> if1Statements = new ListBuffer<JCStatement>();
                if1Statements.append(
                        executeMethod(
                                treeMaker,
                                createFieldAccessor(treeMaker, fieldNode, FieldAccess.ALWAYS_FIELD),
                                sourceNode.toName(
                                        buildAccessorName(
                                                sourceNode,
                                                "set",
                                                associationDetails.inverseSideAnnotation != null
                                                        ? associationDetails.owningSideFieldName
                                                        : associationDetails.inverseSideFieldName
                                        )
                                ),
                                List.<JCExpression>of(
                                        treeMaker.NullLiteral()
                                )
                        )
                );

            ifStatements.append(
                    treeMaker.If(
                            treeMaker.Binary(
                                    CTC_NOT_EQUAL,
                                    createFieldAccessor(treeMaker, fieldNode, FieldAccess.ALWAYS_FIELD),
                                    treeMaker.NullLiteral()
                            ),
                            treeMaker.Block(0, if1Statements.toList()),
                            null
                    )
            );

            ListBuffer<JCStatement> elseStatements = new ListBuffer<JCStatement>();
            elseStatements.append(
                    executeMethod(
                            treeMaker,
                            treeMaker.Ident(param.name),
                            sourceNode.toName(
                                    buildAccessorName(
                                            sourceNode,
                                            "set",
                                            associationDetails.inverseSideAnnotation != null
                                                    ? associationDetails.owningSideFieldName
                                                    : associationDetails.inverseSideFieldName
                                    )
                            ),
                            List.<JCExpression>of(
                                    treeMaker.Ident(fieldNode.toName("this"))
                            )
                    )
            );

        statements.append(
                treeMaker.If(
                        treeMaker.Binary(CTC_EQUAL, treeMaker.Ident(param.name), treeMaker.NullLiteral()),
                        treeMaker.Block(0, ifStatements.toList()),
                        treeMaker.Block(0, elseStatements.toList())
                )
        );
        statements.append(
                treeMaker.Exec(
                        treeMaker.Assign(
                                createFieldAccessor(treeMaker, fieldNode, FieldAccess.ALWAYS_FIELD),
                                treeMaker.Ident(param.name)
                        )
                )
        );

        JCBlock methodBody = treeMaker.Block(0, statements.toList());

        JCMethodDecl methodDecl = treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                sourceNode.toName(methodPrefix + capitalize(sourceNode, param.name.toString())),
                treeMaker.Type(createVoidType(sourceNode.getSymbolTable(), CTC_VOID)),
                List.<JCTypeParameter>nil(),
                parameters.toList(),
                List.<JCExpression>nil(),
                methodBody,
                null
        );

        return recursiveSetGeneratedBy(methodDecl, sourceNode);
    }

    private JCMethodDecl createMethodForOneToManyAssociation(JavacNode fieldNode,
                                                             JavacNode sourceNode,
                                                             JavacTreeMaker treeMaker,
                                                             AssociationDetails associationDetails,
                                                             String methodPrefix) {
        JCVariableDecl fieldDecl = (JCVariableDecl) fieldNode.get();

        JCIdent paramType = getFieldTypeOrGenericType(fieldDecl, sourceNode, treeMaker);
        Name paramName = associationDetails.paramName != null
                ? sourceNode.toName(associationDetails.paramName)
                : sourceNode.toName(uncapitalize(sourceNode, paramType.getName().toString()));
        JCVariableDecl param = createParameter(treeMaker, paramName, paramType);

        ListBuffer<JCVariableDecl> parameters = new ListBuffer<JCVariableDecl>();
        parameters.append(param);

        ListBuffer<JCStatement> statements = new ListBuffer<JCStatement>();
        statements.append(
                executeMethod(
                        treeMaker,
                        createFieldAccessor(treeMaker, fieldNode, FieldAccess.ALWAYS_FIELD),
                        sourceNode.toName(methodPrefix),
                        List.<JCExpression>of(
                                treeMaker.Ident(param.name)
                        )
                )
        );
        statements.append(
                executeMethod(
                        treeMaker,
                        treeMaker.Ident(param.name),
                        sourceNode.toName(
                                buildAccessorName(sourceNode,  "set", associationDetails.owningSideFieldName)
                        ),
                        List.<JCExpression>of(
                                methodPrefix.equals(ADD_METHOD_PREFIX)
                                        ? treeMaker.Ident(sourceNode.toName("this"))
                                        : treeMaker.NullLiteral()
                        )
                )
        );

        JCBlock methodBody = treeMaker.Block(0, statements.toList());

        JCMethodDecl methodDecl = treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                sourceNode.toName(methodPrefix + capitalize(sourceNode, param.name.toString())),
                treeMaker.Type(createVoidType(sourceNode.getSymbolTable(), CTC_VOID)),
                List.<JCTypeParameter>nil(),
                parameters.toList(),
                List.<JCExpression>nil(),
                methodBody,
                null
        );

        return recursiveSetGeneratedBy(methodDecl, sourceNode);
    }

    private JCVariableDecl createParameter(JavacTreeMaker treeMaker,
                                           Name paramName,
                                           JCIdent paramType) {
        return treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PARAMETER),
                paramName,
                paramType,
                null
        );
    }

    private JCExpressionStatement executeMethod(JavacTreeMaker treeMaker,
                                                JCExpression field,
                                                Name methodName,
                                                List<JCExpression> arguments) {
        return treeMaker.Exec(
                treeMaker.Apply(
                        List.<JCExpression>nil(),
                        treeMaker.Select(
                                field,
                                methodName
                        ),
                        arguments
                )
        );
    }

    private JCIdent getFieldTypeOrGenericType(JCVariableDecl fieldDecl,
                                              JavacNode sourceNode,
                                              JavacTreeMaker treeMaker) {
        JCExpression fieldType = fieldDecl.vartype;

        if (fieldType instanceof JCTypeApply) {
            return (JCIdent) cloneType(
                    treeMaker,
                    getGenericTypes((JCTypeApply) fieldType).get(0),
                    sourceNode
            );
        }

        return (JCIdent) cloneType(treeMaker, fieldType, sourceNode);
    }

    private List<AssociationDetails> getAssociationDetailsList(JavacNode fieldNode,
                                                               JavacNode sourceNode,
                                                               JavacTreeMaker treeMaker,
                                                               List<Class<? extends Annotation>> annotationClasses) {
        JCIdent fieldType = getFieldTypeOrGenericType((JCVariableDecl) fieldNode.get(), sourceNode, treeMaker);

        AnnotationValues<JpaAssociationSync.Extra> extraSettings = fieldNode.findAnnotation(JpaAssociationSync.Extra.class);

        List<AnnotationValues<? extends Annotation>> associationAnnotations = getAssociationAnnotations(fieldNode, annotationClasses);

        ListBuffer<AssociationDetails> associationDetailsListBuffer = new ListBuffer<AssociationDetails>();
        for (AnnotationValues<? extends Annotation> annotation : associationAnnotations) {
            if (AssociationDetails.isOwningSide(annotation)) {
                if (extraSettings != null
                        && !extraSettings.getAsString("inverseSideFieldName").isEmpty()) {
                    AssociationDetails associationDetails = new AssociationDetails();

                    associationDetails.owningSideAnnotation = annotation;

                    associationDetails.owningSideFieldName = uncapitalize(sourceNode, fieldType.getName().toString());
                    associationDetails.inverseSideFieldName = extraSettings.getAsString("inverseSideFieldName");

                    associationDetails.paramName =
                            !extraSettings.getAsString("paramName").isEmpty()
                                    ? extraSettings.getAsString("paramName")
                                    : null;

                    associationDetailsListBuffer.append(associationDetails);
                }
            } else {
                AssociationDetails associationDetails = new AssociationDetails();

                associationDetails.inverseSideAnnotation = annotation;

                associationDetails.owningSideFieldName = AssociationDetails.getOwningSideFieldName(annotation);
                associationDetails.inverseSideFieldName = uncapitalize(sourceNode, fieldType.getName().toString());

                associationDetails.paramName =
                        extraSettings != null && !extraSettings.getAsString("paramName").isEmpty()
                                ? extraSettings.getAsString("paramName")
                                : null;

                associationDetailsListBuffer.append(associationDetails);
            }
        }

        return associationDetailsListBuffer.toList();
    }

    private List<AnnotationValues<? extends Annotation>> getAssociationAnnotations(JavacNode fieldNode,
                                                                                   List<Class<? extends Annotation>> annotationClasses) {
        ListBuffer<AnnotationValues<? extends Annotation>> annotations = new ListBuffer<AnnotationValues<?>>();

        for (Class<? extends Annotation> annotationClass : annotationClasses) {
            if (annotationClass != null) {
                AnnotationValues<? extends Annotation> annotation = fieldNode.findAnnotation(annotationClass, false);

                if (annotation != null) {
                    annotations.append(annotation);
                }
            }
        }

        return annotations.toList();
    }

    private Class<? extends Annotation> findAnnotationClass(String className) {
        try {
            return Class.forName(className).asSubclass(Annotation.class);
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
        AnnotationValues<? extends Annotation> owningSideAnnotation;
        AnnotationValues<? extends Annotation> inverseSideAnnotation;

        String owningSideFieldName;
        String inverseSideFieldName;

        String paramName;

        AssociationDetails() {
        }

        AssociationDetails(String owningSideFieldName, String inverseSideFieldName) {
            this.owningSideFieldName = owningSideFieldName;
            this.inverseSideFieldName = inverseSideFieldName;
        }

        Class<? extends Annotation> getType() {
            if (owningSideAnnotation != null)
                return owningSideAnnotation.getType();
            if (inverseSideAnnotation != null)
                return inverseSideAnnotation.getType();

            return null;
        }

        static String getOwningSideFieldName(AnnotationValues<? extends Annotation> inverseSideAnnotation) {
            return inverseSideAnnotation.getAsString(MAPPED_BY_METHOD_NAME);
        }

        static boolean isOwningSide(AnnotationValues<? extends Annotation> annotation) {
            return annotation.getAsString(MAPPED_BY_METHOD_NAME).isEmpty();
        }

        static boolean isInverseSide(AnnotationValues<? extends Annotation> annotation) {
            return !isOwningSide(annotation);
        }
    }
}
