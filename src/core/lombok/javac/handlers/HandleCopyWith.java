/*
 * Copyright (C) 2010-2025 The Project Lombok Authors.
 *
 * Licensed under the MIT License (same as other lombok source files).
 */
package lombok.javac.handlers;

import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.ListBuffer;

import lombok.AccessLevel;
import lombok.CopyWith;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.handlers.JavacHandlerUtil;

import static lombok.javac.handlers.JavacHandlerUtil.*;

/**
 * Handles the {@link lombok.CopyWith} annotation for javac.
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
public class HandleCopyWith extends JavacAnnotationHandler<CopyWith> {

    @Override
    public void handle(AnnotationValues<CopyWith> annotation, JCAnnotation ast, JavacNode annotationNode) {
        JavacNode typeNode = annotationNode.up();

        if (typeNode == null || typeNode.getKind() != JavacNode.Kind.TYPE) {
            annotationNode.addError("@CopyWith is only supported on types.");
            return;
        }

        CopyWith copyWithInstance = annotation.getInstance();
        AccessLevel level = copyWithInstance.access();

        generateCopyWith(typeNode, level, annotationNode, ast);
    }

    private void generateCopyWith(JavacNode typeNode, AccessLevel level, JavacNode source, JCAnnotation ast) {
        // read the fields of the class
        java.util.List<JavacNode> fields = fieldsOf(typeNode);

        if (fields.isEmpty()) {
            source.addWarning("No fields found in class, no copyWith generated.");
            return;
        }

        // build method parameters and constructor arguments
        ListBuffer<JCVariableDecl> params = new ListBuffer<>();
        ListBuffer<JCExpression> constructorArgs = new ListBuffer<>();

        for (JavacNode field : fields) {
            JCVariableDecl decl = (JCVariableDecl) field.get();
            params.append(treeMaker(typeNode).VarDef(
                    treeMaker(typeNode).Modifiers(0),
                    decl.name,
                    decl.vartype,
                    null
            ));


            //If no null/zero value is provided → use the original field value
            JCExpression replacement = treeMaker(typeNode).Conditional(
                    treeMaker(typeNode).Binary(JCTree.Tag.NE,
                            treeMaker(typeNode).Ident(decl.name),
                            literalNull(typeNode)),
                    treeMaker(typeNode).Ident(decl.name),
                    treeMaker(typeNode).Select(treeMaker(typeNode).Ident(typeNode.toName("this")), decl.name)
            );

            constructorArgs.append(replacement);
        }

        // method body → return new ClassName(args...)
        JCExpression newClassExpr = treeMaker(typeNode).NewClass(
                null, nil(), namePlusType(typeNode), constructorArgs.toList(), null
        );

        JCBlock body = treeMaker(typeNode).Block(0, com.sun.tools.javac.util.List.of(
                treeMaker(typeNode).Return(newClassExpr)
        ));

        JCMethodDecl copyWithMethod = treeMaker(typeNode).MethodDef(
                treeMaker(typeNode).Modifiers(toJavacModifier(level)),
                typeNode.toName("copyWith"),
                namePlusType(typeNode),
                nil(),
                params.toList(),
                nil(),
                body,
                null
        );

        injectMethod(typeNode, copyWithMethod);
    }
}
