package lombok.javac.handlers;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import lombok.core.AST;
import lombok.core.AnnotationValues;
import lombok.experimental.Memoize;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.spi.Provides;

import static lombok.javac.handlers.JavacHandlerUtil.*;

@Provides
public class HandleMemoize extends JavacAnnotationHandler<Memoize> {

    @Override
    public void handle(AnnotationValues<Memoize> annotation, JCTree.JCAnnotation ast, JavacNode annotationNode) {
        // TODO: handle experimental flag usage

        deleteAnnotationIfNeccessary(annotationNode, Memoize.class);

        JavacNode annotatedNode = annotationNode.up();
        if (annotatedNode.getKind() != AST.Kind.METHOD) {
            annotationNode.addError("@Memoize is only valid on a method.");
            return;
        }
        JCTree.JCMethodDecl md = (JCTree.JCMethodDecl) annotatedNode.get();

        JavacNode containerNode = annotatedNode.up();
        if (containerNode.getKind() != AST.Kind.TYPE) {
            annotationNode.addError("@Memoize is only valid on methods in a class.");
            return;
        }
        JCTree.JCClassDecl cd = (JCTree.JCClassDecl) containerNode.get();

        if ((md.mods.flags & Flags.ABSTRACT) != 0) {
            annotationNode.addError("@Memoize is not valid on an abstract method.");
            return;
        }

        if (md.name.charAt(0) == '<') {
            annotationNode.addError("@Memoize is not valid on a constructor.");
        }

        boolean isStatic = (md.mods.flags & Flags.STATIC) != 0;

        JavacTreeMaker maker = containerNode.getTreeMaker();
        Name cacheName = annotationNode.toName("$lombok$memoizeCache$" + cd.name.toString() + "$" + md.name.toString());
        JCTree.JCVariableDecl cacheVariable = maker.VarDef(
                maker.Modifiers(Flags.PRIVATE | Flags.TRANSIENT | (isStatic ? Flags.STATIC : 0)),
                cacheName,
                chainDots(annotatedNode, "java", "util", "Map"),
                maker.NewClass(null, List.<JCTree.JCExpression>nil(), chainDots(annotatedNode, "java", "util", "HashMap"), List.<JCTree.JCExpression>nil(), null)
        );
        injectFieldAndMarkGenerated(containerNode, cacheVariable);
        recursiveSetGeneratedBy(cacheVariable, annotationNode);


        Name newName = annotatedNode.toName(md.name.toString() + "$uncached");
        Name keyName = annotatedNode.toName("key");
        Name resultName = annotatedNode.toName("result");

        ListBuffer<JCTree.JCExpression> paramNames = new ListBuffer<JCTree.JCExpression>();
        for (JCTree.JCVariableDecl param : md.params) {
            paramNames.add(maker.Ident(param.name));
        }

        ListBuffer<JCTree.JCExpression> tyParamNames = new ListBuffer<JCTree.JCExpression>();
        for (JCTree.JCTypeParameter param : md.typarams) {
            tyParamNames.add(maker.Ident(param.name));
        }


        List<JCTree.JCStatement> newStatements = List.<JCTree.JCStatement>of(
                maker.VarDef(maker.Modifiers(0), keyName, genJavaLangTypeRef(annotatedNode, ast.pos, "Object"), maker.Apply(
                        List.<JCTree.JCExpression>nil(), chainDots(annotatedNode, "java", "util", "Arrays", "asList"), List.<JCTree.JCExpression>of(
                                maker.NewArray(
                                        genJavaLangTypeRef(annotatedNode, ast.pos, "Object"),
                                        List.<JCTree.JCExpression>nil(),
                                        paramNames.toList()
                                )
                        )
                )),
                maker.If(
                        maker.Apply(
                                List.<JCTree.JCExpression>nil(), chainDots(annotatedNode, cacheName.toString(), "containsKey"), List.<JCTree.JCExpression>of(maker.Ident(keyName))
                        ),
                        maker.Return(maker.TypeCast(
                                md.restype,
                                maker.Apply(
                                        List.<JCTree.JCExpression>nil(), chainDots(annotatedNode, cacheName.toString(), "get"), List.<JCTree.JCExpression>of(maker.Ident(keyName))
                                )
                        )),
                        maker.Block(0, List.<JCTree.JCStatement>of(
                                maker.VarDef(maker.Modifiers(0), resultName, md.restype,
                                        maker.Apply(tyParamNames.toList(), maker.Select(isStatic ? maker.Ident(cd.name) : maker.Ident(annotatedNode.toName("this")), newName), paramNames.toList())
                                ),
                                maker.Exec(maker.Apply(List.<JCTree.JCExpression>nil(), chainDots(annotatedNode, cacheName.toString(), "put"), List.<JCTree.JCExpression>of(maker.Ident(keyName), maker.Ident(resultName)))),
                                maker.Return(maker.Ident(resultName))
                        ))
                )
        );

        ListBuffer<JCTree.JCAnnotation> newAnnotations = new ListBuffer<JCTree.JCAnnotation>();
        for (JCTree.JCAnnotation jcAnnotation : md.mods.annotations) {
            if (jcAnnotation != annotationNode.get())
                newAnnotations.add(jcAnnotation);
        }
        md.mods.annotations = newAnnotations.toList();
        JCTree.JCMethodDecl newMethod = maker.MethodDef(maker.Modifiers((md.mods.flags & ~Flags.PUBLIC & ~Flags.PROTECTED) | Flags.PRIVATE, List.<JCTree.JCAnnotation>nil()), newName, md.restype, md.typarams, md.params, md.thrown, md.body, null);
        injectMethod(containerNode, newMethod);
        md.body = maker.Block(0, newStatements);
    }
}
