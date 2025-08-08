package lombok.javac.handlers;

import static lombok.javac.handlers.JavacHandlerUtil.*;

import lombok.Destructure;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.spi.Provides;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

@Provides
public class HandleDestructure extends JavacAnnotationHandler<Destructure> {

    @Override
    public void handle(AnnotationValues<Destructure> annotation, JCAnnotation ast, JavacNode annotationNode) {
        deleteAnnotationIfNeccessary(annotationNode, Destructure.class);

        if (annotationNode.up().getKind() != Kind.LOCAL) {
            annotationNode.addError("@Destructure is legal only on local variable declarations.");
            return;
        }

        String[] fields = annotation.getInstance().value();
        if (fields == null || fields.length == 0) {
            annotationNode.addError("@Destructure requires at least one field name.");
            return;
        }

        JCVariableDecl targetLocal = (JCVariableDecl) annotationNode.up().get();
        if (targetLocal.init == null) {
            annotationNode.addError("@Destructure variable must be initialized to a value.");
            return;
        }

        JavacNode parent = annotationNode.up().directUp();
        JCTree block = parent.get();
        List<JCStatement> statements;
        if (block instanceof JCBlock) statements = ((JCBlock) block).stats;
        else if (block instanceof JCCase) statements = ((JCCase) block).stats;
        else if (block instanceof JCTree.JCMethodDecl) statements = ((JCTree.JCMethodDecl) block).body.stats;
        else {
            annotationNode.addError("@Destructure must be used inside a block.");
            return;
        }

        boolean found = false;
        ListBuffer<JCStatement> newStatements = new ListBuffer<JCStatement>();
        for (JCStatement st : statements) {
            newStatements.append(st);
            if (!found && st == targetLocal) {
                found = true;
                for (String field : fields) {
                    JCVariableDecl decl = createLocalFromGetter(annotationNode, targetLocal.name, field);
                    if (decl != null) newStatements.append(decl);
                }
            }
        }

        if (!found) {
            annotationNode.addError("LOMBOK BUG: Can't find this local variable declaration inside its parent.");
            return;
        }

        if (block instanceof JCBlock) ((JCBlock) block).stats = newStatements.toList();
        else if (block instanceof JCCase) ((JCCase) block).stats = newStatements.toList();
        else if (block instanceof JCTree.JCMethodDecl) ((JCTree.JCMethodDecl) block).body.stats = newStatements.toList();

        parent.rebuild();
    }

    private JCVariableDecl createLocalFromGetter(JavacNode node, Name targetVar, String field) {
        JavacTreeMaker maker = node.getTreeMaker();
        String cap = capitalize(field);
        Name getterName = node.toName("get" + cap);

        JCExpression selectTarget = maker.Ident(targetVar);
        JCMethodInvocation call = maker.Apply(List.<JCExpression>nil(), maker.Select(selectTarget, getterName), List.<JCExpression>nil());

        JCExpression vartypeExpr = genJavaLangTypeRef(node, "Object");
        JCVariableDecl decl = maker.VarDef(maker.Modifiers(0L), node.toName(field), vartypeExpr, call);
        recursiveSetGeneratedBy(decl, node);
        return decl;
    }

    private static String capitalize(String in) {
        if (in == null || in.isEmpty()) return in;
        char first = in.charAt(0);
        char upper = Character.toUpperCase(first);
        if (first == upper) return in;
        return upper + in.substring(1);
    }
}