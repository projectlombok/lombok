/*
 * Copyright (C) 2010-2025 The Project Lombok Authors.
 *
 * Licensed under the MIT License (same as other lombok source files).
 */
package lombok.eclipse.handlers;

import lombok.AccessLevel;
import lombok.CopyWith;
import lombok.core.AnnotationValues;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

/**
 * Handles the {@link lombok.CopyWith} annotation for Eclipse.
 * <p>
 * Generates a <code>copyWith(...)</code> method for the annotated class
 * that creates a new instance with optionally replaced field values.
 *
 * <h2>Example</h2>
 * <pre>
 * {@code
 * @CopyWith
 * class Person {
 *     private final String name;
 *     private final int age;
 * }
 * }
 * </pre>
 *
 * Generates (conceptually):
 * <pre>
 * {@code
 * public Person copyWith(String name, int age) {
 *     return new Person(
 *         name != null ? name : this.name,
 *         age != 0 ? age : this.age
 *     );
 * }
 * }
 * </pre>
 */
@lombok.core.AnnotationHandlerFor(CopyWith.class)
public class HandleCopyWith extends EclipseAnnotationHandler<CopyWith> {

    @Override
    public void handle(AnnotationValues<CopyWith> annotation, Annotation ast, EclipseNode annotationNode) {
        EclipseNode typeNode = annotationNode.up();
        if (typeNode == null || typeNode.getKind() != EclipseNode.Kind.TYPE) {
            annotationNode.addError("@CopyWith is only supported on types.");
            return;
        }

        CopyWith copyWith = annotation.getInstance();
        AccessLevel level = copyWith.access();

        generateCopyWith(typeNode, level, annotationNode, ast);
    }

    private void generateCopyWith(EclipseNode typeNode, AccessLevel level, EclipseNode source, Annotation ast) {
        // get all fields of the class
        java.util.List<EclipseNode> fields = fieldsOf(typeNode);
        if (fields.isEmpty()) {
            source.addWarning("No fields found in class, no copyWith generated.");
            return;
        }

        TypeDeclaration typeDecl = (TypeDeclaration) typeNode.get();
        ASTNode sourceAst = source.get();


        //build parameters and constructor arguments
        Argument[] params = new Argument[fields.size()];
        Expression[] constructorArgs = new Expression[fields.size()];

        for (int i = 0; i < fields.size(); i++) {
            FieldDeclaration fieldDecl = (FieldDeclaration) fields.get(i).get();

            char[] paramName = fieldDecl.name;
            params[i] = new Argument(paramName, 0, copyType(fieldDecl.type, sourceAst), ClassFileConstants.AccFinal);

            //if not null → use the parameter value
            Expression cond = EclipseHandlerUtil.makeNullCheck(paramName, copyType(fieldDecl.type, sourceAst));
            Expression fallback = new FieldReference(fieldDecl.name, 0L);
            fallback.receiver = new ThisReference(0, 0);

            constructorArgs[i] = new ConditionalExpression(
                    new SingleNameReference(paramName, 0L),
                    new SingleNameReference(paramName, 0L),
                    fallback
            );
        }

        // new ClassName(args...)
        AllocationExpression constructorCall = new AllocationExpression();
        constructorCall.type = EclipseHandlerUtil.makeType(typeDecl.name, sourceAst);
        constructorCall.arguments = constructorArgs;

        // return statement
        ReturnStatement returnStmt = new ReturnStatement(constructorCall, 0, 0);

        // body
        Statement[] statements = new Statement[]{ returnStmt };

        // copyWith method
        MethodDeclaration copyWithMethod = new MethodDeclaration(typeDecl.compilationResult);
        copyWithMethod.modifiers = toEclipseModifier(level);
        copyWithMethod.returnType = EclipseHandlerUtil.makeType(typeDecl.name, sourceAst);
        copyWithMethod.selector = "copyWith".toCharArray();
        copyWithMethod.arguments = params;
        copyWithMethod.bodyStart = copyWithMethod.declarationSourceStart = sourceAst.sourceStart;
        copyWithMethod.bodyEnd = copyWithMethod.declarationSourceEnd = sourceAst.sourceEnd;
        copyWithMethod.statements = statements;

        //inject method to class
        injectMethod(typeNode, copyWithMethod);
    }
}
