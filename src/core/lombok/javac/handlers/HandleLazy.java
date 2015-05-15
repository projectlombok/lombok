/*
 * Copyright (C) 2013-2015 The Project Lombok Authors.
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

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import lombok.Lazy;
import lombok.core.AnnotationValues;
import lombok.javac.Javac;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import org.mangosdk.spi.ProviderFor;

import java.lang.reflect.Modifier;
import java.util.Iterator;

@ProviderFor(JavacAnnotationHandler.class)
@SuppressWarnings("restriction")
public final class HandleLazy extends JavacAnnotationHandler<Lazy> {
    @Override
    public void handle(
            AnnotationValues<Lazy> annotation,
            JCTree.JCAnnotation ast,
            JavacNode annotationNode
    ) {
        new AnnotatedMethod(annotationNode, ast).transformToLazy();
        JavacHandlerUtil.deleteAnnotationIfNeccessary(annotationNode, Lazy.class);
    }

    private class AnnotatedMethod {
        private final JavacNode annotationNode;
        private final JCTree.JCAnnotation ast;
        private final Name originalMethodName;
        private final JCTree.JCMethodDecl originalMethod;
        private final JavacNode methodNode;
        private final long originalMethodFlags;
        private JCTree.JCVariableDecl storageField;

        public AnnotatedMethod(
                JavacNode annotationNode,
                JCTree.JCAnnotation ast
        ) {
            this.annotationNode = annotationNode;
            this.methodNode = annotationNode.up();
            this.ast = ast;
            this.originalMethod = ((JCTree.JCMethodDecl) methodNode.get());
            this.originalMethodName = originalMethod.name;
            this.storageField = StorageField.createField(methodNode);
            this.originalMethodFlags = originalMethod.mods.flags;
        }

        public void transformToLazy() {
            addField();
            putOriginalMethodBodyInNewMethod();
            replaceOriginalMethodBodyWithLazyInit();
        }


        private void putOriginalMethodBodyInNewMethod() {
            JavacHandlerUtil.injectMethod(
                    getClassNode(),
                    treeMaker().MethodDef(
                            treeMaker().Modifiers(
                                    Modifier.PRIVATE
                            ),
                            movedBehaviorMethodName(),
                            originalMethod.restype,
                            originalMethod.typarams,
                            originalMethod.params,
                            originalMethod.thrown,
                            originalMethod.body,
                            originalMethod.defaultValue
                    )
            );
        }

        private JCTree.JCBlock lazyInitBody() {
            JavacTreeMaker maker = treeMaker();
            JCTree.JCIdent storageFieldIdent = maker.Ident(storageField.name);
            return maker.Block(
                    0,
                    List.of(
                            maker.If(
                                    maker.Binary(
                                            Javac.CTC_EQUAL,
                                            storageFieldIdent,
                                            maker.Literal(
                                                    Javac.CTC_BOT,
                                                    null
                                            )
                                    ),
                                    maker.Exec(
                                            maker.Assign(
                                                    storageFieldIdent,
                                                    createBehaviorMethodApplication()
                                            )
                                    ),
                                    null
                            ),
                            maker.Return(storageFieldIdent)
                    )
            );
        }

        private JCTree.JCExpression createBehaviorMethodApplication() {
            JavacTreeMaker maker = treeMaker();
            return maker.Apply(
                    List.<JCTree.JCExpression>nil(),
                    maker.Ident(movedBehaviorMethodName()),
                    List.<JCTree.JCExpression>nil()
            );
        }

        private void removeGeneratedAnnotations(JCTree.JCMethodDecl lazyMethod) {
            Iterator<JCTree.JCAnnotation> iterator = lazyMethod.mods.annotations.iterator();
            List<JCTree.JCAnnotation> annotationList = List.nil();
            while (iterator.hasNext()) {
                JCTree.JCAnnotation next = iterator.next();
                String annotationTypeName = next.getAnnotationType().toString();
                if (
                        !annotationTypeName.equals("javax.annotation.Generated")
                                && !annotationTypeName.equals("java.lang.SuppressWarnings")
                        ) {
                    annotationList.add(next);
                }
            }
            lazyMethod.mods.annotations = annotationList;
        }


        private JavacTreeMaker treeMaker() {
            return annotationNode.getTreeMaker();
        }

        private void replaceOriginalMethodBodyWithLazyInit() {
//            originalMethod.name = movedBehaviorMethodName();
//            originalMethod.mods = treeMaker().Modifiers(Modifier.PRIVATE);
            originalMethod.body = lazyInitBody();
        }

        private void addField() {
            JavacHandlerUtil.injectFieldAndMarkGenerated(
                    getClassNode(),
                    storageField
            );
        }

        private JavacNode getClassNode() {
            return methodNode.up();
        }

        private Name movedBehaviorMethodName() {
            return methodNode.toName("$behavior$").append(originalMethodName);
        }
    }

    private static class StorageField {

        private static JCTree.JCModifiers mods(JavacNode methodNode) {
            return methodNode.getTreeMaker().Modifiers(Modifier.PRIVATE);
        }

        private static Name name(JavacNode methodNode) {
            return methodNode.toName("$lazy$" + methodNode.getName());
        }

        private static JCTree.JCExpression vartype(JavacNode methodNode) {
            return ((JCTree.JCMethodDecl) methodNode.get()).restype;
        }

        private static JCTree.JCExpression init(JavacNode methodNode) {
            return null;
        }

        public static JCTree.JCVariableDecl createField(JavacNode methodNode) {
            return methodNode.getTreeMaker().VarDef(
                    mods(methodNode),
                    name(methodNode),
                    vartype(methodNode),
                    init(methodNode)
            );
        }
    }
}