/*
 * Copyright (C) 2023 The Project Lombok Authors.
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

import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import lombok.ConfigurationKeys;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.experimental.Generator;
import lombok.javac.JavacAST;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.tree.JCTree.*;
import lombok.spi.Provides;

@Provides
public class HandleGenerator extends JavacAnnotationHandler<Generator> {
    @Override public void handle(AnnotationValues<Generator> annotation, JCAnnotation ast, JavacNode annotationNode) {
        handleExperimentalFlagUsage(annotationNode, ConfigurationKeys.GENERATOR_FLAG_USAGE, "@Generator");
        deleteAnnotationIfNeccessary(annotationNode, Generator.class);

        JavacNode methodNode = annotationNode.up();
        if (methodNode.getKind() != Kind.METHOD) {
            annotationNode.addError("@Generator can only be applied to method");
            return;
        }

        intoGeneratorMethod(methodNode);
        recursiveSetGeneratedBy(methodNode.get(), annotationNode);
        methodNode.rebuild();
    }

    private static JCExpression extractIteratorType(JavacNode node, JCExpression type) {
        if (type instanceof JCTypeApply) {
            return ((JCTypeApply) type).arguments.head;
        }

        return genJavaLangTypeRef(node, "Object");
    }

    private static void checkMethod(final JavacNode node, final String innerClassName) {
        JCMethodDecl methodDecl = (JCMethodDecl) node.get();

        if (!methodDecl.thrown.isEmpty()) {
            node.addWarning("throws in generator method does nothing", methodDecl.pos());
        }

        if ((methodDecl.mods.flags & Flags.SYNCHRONIZED) != 0) {
            node.addError("Generator method cannot be synchronized", methodDecl.pos());
        }

        methodDecl.accept(new TreeScanner() {
            private int synchronizedDepth = 0;

            @Override public void visitSynchronized(JCSynchronized arg0) {
                synchronizedDepth++;
                super.visitSynchronized(arg0);
                synchronizedDepth--;
            }

            // Ignore inner class
            @Override public void visitClassDef(JCClassDecl arg0) {}

            @Override public void visitIdent(JCIdent arg0) {
                if (innerClassName.equals(arg0.name.toString())) {
                    node.addError(innerClassName + " is used internally in generater method", arg0.pos());
                }

                super.visitIdent(arg0);
            }

            @Override public void visitApply(JCMethodInvocation arg0) {
                String name = arg0.meth.toString();
                if (synchronizedDepth > 0 && ("yieldThis".equals(name) || "yieldAll".equals(name))) {
                    node.addError("Cannot yield inside synchronized block", arg0.pos());
                }

                if (arg0.meth instanceof JCIdent && "advance".equals(name) && arg0.args.isEmpty()) {
                    node.addError("Cannot call advance method directly in generater method", arg0.pos());
                }

                super.visitApply(arg0);
            }
        });
    }

    public static void intoGeneratorMethod(JavacNode sourceMethod) {
        JCMethodDecl methodDecl = (JCMethodDecl) sourceMethod.get();
        JCClassDecl classDecl = (JCClassDecl) sourceMethod.up().get();
        JavacAST ast = sourceMethod.getAst();
        JavacTreeMaker treeMaker = ast.getTreeMaker();

        String className = "__Generator";

        checkMethod(sourceMethod, className);

        JCClassDecl generatorDecl = createGeneratorClass(
            sourceMethod,
            treeMaker.Ident(classDecl.name),
            className,
            extractIteratorType(sourceMethod, methodDecl.restype),
            methodDecl.body
        );

        JCBlock methodBody = treeMaker.Block(0, List.of(
            generatorDecl,
            treeMaker.Return(
                treeMaker.NewClass(
                    (JCExpression) null,
                    List.<JCExpression>nil(),
                    treeMaker.Ident(generatorDecl.name),
                    List.<JCExpression>nil(),
                    (JCClassDecl) null
                )
            )
        ));

        methodDecl.body = methodBody;
    }

    public static JCClassDecl createGeneratorClass(
        JavacNode node,
        JCExpression classExpr,
        String name,
        JCExpression type,
        JCBlock block
    ) {
        JavacAST ast = node.getAst();
        JavacTreeMaker treeMaker = ast.getTreeMaker();

        ListBuffer<JCTree> bodyBuffer = new ListBuffer<JCTree>();

        remapThisSuper(ast, classExpr, block);
        JCMethodDecl advanceDecl = treeMaker.MethodDef(
            treeMaker.Modifiers(Flags.PROTECTED),
            ast.toName("advance"),
            treeMaker.TypeIdent(CTC_VOID),
            List.<JCTypeParameter>nil(),
            List.<JCVariableDecl>nil(),
            List.<JCExpression>nil(),
            block,
            null
        );
        bodyBuffer.append(advanceDecl);

        return treeMaker.ClassDef(
            treeMaker.Modifiers(Flags.FINAL),
            ast.toName(name),
            List.<JCTypeParameter>nil(),
            treeMaker.TypeApply(chainDots(node, "lombok", "Lombok", "Generator"), List.of(type)),
            List.<JCExpression>nil(),
            bodyBuffer.toList()
        );
    }

    /**
     * Remap this, super selection to select outer class
     */
    private static <T extends JCTree> void remapThisSuper(final JavacAST ast, final JCExpression classExpr, T tree) {
        tree.accept(new TreeScanner() {
            @Override public void visitSelect(JCFieldAccess arg0) {
                if (arg0.selected instanceof JCIdent) {
                    String selected = arg0.selected.toString();
                    if ("this".equals(selected) || "super".equals(selected)) {
                        JavacTreeMaker treeMaker = ast.getTreeMaker().at(arg0.pos);
                        arg0.selected = treeMaker.Select(classExpr, ((JCIdent) arg0.selected).name);
                    }
                }
            }
        });
    }
}
