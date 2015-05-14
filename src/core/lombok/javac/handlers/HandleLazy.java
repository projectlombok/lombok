package lombok.javac.handlers;

import com.sun.tools.javac.code.Symbol;
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

@ProviderFor(JavacAnnotationHandler.class)
@SuppressWarnings("restriction")
public final class HandleLazy extends JavacAnnotationHandler<Lazy> {
    @Override
    public void handle(
            AnnotationValues<Lazy> annotation,
            JCTree.JCAnnotation ast,
            JavacNode annotationNode
    ) {
        new AnnotatedMethod(annotationNode).addLaziness();
    }

    private class AnnotatedMethod {
        private final JavacNode annotationNode;
        private final Name originalMethodName;
        private final JCTree.JCMethodDecl renamedBehaviorMethod;
        private StorageField storageField;

        public AnnotatedMethod(
                JavacNode annotationNode
        ) {
            this.annotationNode = annotationNode;
            this.renamedBehaviorMethod = ((JCTree.JCMethodDecl) annotationNode.up().get());
            this.originalMethodName = renamedBehaviorMethod.name;
            this.storageField = new StorageField(annotationNode);
        }

        public void addLaziness() {
            addField();
//            renameOriginalMethod();
//            addLazyMethodInPlaceOfOriginal();
        }

        private void addLazyMethodInPlaceOfOriginal() {
            JavacHandlerUtil.injectMethod(
                    getClassNode(),
                    LazyMethod.createLazyMethod(
                            this,
                            storageField
                    )
            );
        }

        private JavacTreeMaker treeMaker() {
            return annotationNode.getTreeMaker();
        }

        private void renameOriginalMethod() {
            renamedBehaviorMethod.name = movedBehaviorMethodName();
        }

        private void addField() {
            JavacHandlerUtil.injectField(
                    getClassNode(),
                    storageField
            );
        }

        private JavacNode getClassNode() {
            return annotationNode.up().up();
        }

        private Name movedBehaviorMethodName() {
            Name $behavior$ = annotationNode.toName("$behavior$").append(originalMethodName);
            if ($behavior$ == null) {
                throw new NullPointerException();
            }
            return $behavior$;
        }
    }

    private static class StorageField extends JCTree.JCVariableDecl {
        public StorageField(JavacNode annotationNode) {
            super(
                    mods(annotationNode),
                    name(annotationNode),
                    vartype(annotationNode),
                    init(annotationNode),
                    sym(annotationNode)
            );
        }

        private static JCModifiers mods(JavacNode annotationNode) {
            return annotationNode.getTreeMaker().Modifiers(Modifier.PRIVATE);
        }

        private static Name name(JavacNode annotationNode) {
            return annotationNode.toName("$lazy$" + annotationNode.up().getName());
        }

        private static JCExpression vartype(JavacNode annotationNode) {
            return annotationNode.getTreeMaker().Type(
                    annotationNode.up().get().type
            );
        }

        private static JCExpression init(JavacNode annotationNode) {
            return null;
        }

        private static Symbol.VarSymbol sym(JavacNode annotationNode) {
            // TODO: What should I create here?
            return null;
        }
    }

    private static class LazyMethod {

        static JCTree.JCMethodDecl createLazyMethod(
                AnnotatedMethod originalMethod,
                StorageField storageField
        ) {
            return originalMethod.treeMaker().MethodDef(
                    originalMethod.renamedBehaviorMethod.mods,
                    originalMethod.originalMethodName,
                    originalMethod.renamedBehaviorMethod.restype,
                    originalMethod.renamedBehaviorMethod.typarams,
                    originalMethod.renamedBehaviorMethod.params,
                    originalMethod.renamedBehaviorMethod.thrown,
                    body(originalMethod, storageField),
                    originalMethod.renamedBehaviorMethod.defaultValue
                    );
        }

        private static JCTree.JCBlock body(AnnotatedMethod originalMethod, StorageField storageField) {
            JavacTreeMaker maker = originalMethod.treeMaker();
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
                                                    createMethodApplication(originalMethod)
                                            )
                                    ),
                                    null
                            ),
                            maker.Return(storageFieldIdent)
                    )
            );
        }

        private static JCTree.JCExpression createMethodApplication(AnnotatedMethod originalMethod) {
            JavacTreeMaker maker = originalMethod.treeMaker();
            return maker.Apply(
                    List.<JCTree.JCExpression>nil(),
                    maker.Ident(
//                                    getLangThisName(originalMethod)
                            originalMethod.renamedBehaviorMethod.name
                    ),
                    List.<JCTree.JCExpression>nil()
            );
        }

        private static Name getLangThisName(AnnotatedMethod originalMethod) {
            return originalMethod.renamedBehaviorMethod.getName().table._this;
        }
    }

}