package lombok.javac.handlers;

import static lombok.javac.handlers.JavacHandlerUtil.deleteAnnotationIfNeccessary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import lombok.Onstruct;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.core.LombokNode;
import lombok.core.handlers.OnstructUtils;
import lombok.eclipse.DeferUntilPostDiet;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.spi.Provides;

@Provides
@DeferUntilPostDiet
@HandlerPriority(65536) // same as HandleValue // TODO
public class HandleOnstruct extends JavacAnnotationHandler<Onstruct> {
	
	
	/**
	 * find the siblings with same kind and annotation. Copy of
	 * {@link LombokNode#upFromAnnotationToFields()} with same kind and no check
	 * on the parent.
	 *
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static Collection<JavacNode> upFromAnnotationToSameKind(JavacNode node) {
		if (node.getKind() != Kind.ANNOTATION) return Collections.emptyList();
		JavacNode declaration = node.up();
		if (declaration == null) return Collections.emptyList();

		List<JavacNode> fields = new ArrayList();

		for (JavacNode potentialField : declaration.up().down()) {
			if (potentialField.getKind() != declaration.getKind()) continue;
			for (JavacNode child : potentialField.down()) {
				if (child.getKind() != Kind.ANNOTATION) continue;
				if (child.get() == node.get()) fields.add(potentialField);
			}
		}
		
		return fields;
	}

	/**
	 * retrieve the children statements from a list of node
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected static List<JCTree> findChildrenStatements(Collection<JavacNode> fields) {
		List<JCTree> ret = new ArrayList();
		for (JavacNode f : fields) {
			for (JavacNode potentialStatement : f.down()) {
				if (potentialStatement.getKind() == Kind.STATEMENT) {
					ret.add(potentialStatement.get());
				}
			}
		}
		return ret;
	}
	
	@Override public void handle(AnnotationValues<Onstruct> annotation, JCAnnotation ast, JavacNode annotationNode) {
		Collection<JavacNode> annotatedVariables = upFromAnnotationToSameKind(annotationNode);
		JavacNode parentNode = annotationNode.up();
		Onstruct annotationInstance = annotation.getInstance();
		deleteAnnotationIfNeccessary(annotationNode, Onstruct.class);

		List<JCTree> statements = findChildrenStatements(annotatedVariables);
		// sanity checks on statements. Among the variables declaration, there
		// must be exactly one statement.
		if (statements.isEmpty()) {
			annotationNode.addError("no assignment. Requires one identifier assignment.");
			return;
		}
		if (statements.size() > 1) {
			annotationNode.addError("Too many assignments:" + statements + " Requires exactly one identifier assignment.");
			return;
		}

		JCTree tree = statements.get(0);
		JCTree.JCIdent ident;
		String varName = null;
		// sanity checks on the assignment. It must be an identifier.
		if (tree instanceof JCTree.JCIdent) {
			ident = (JCTree.JCIdent) tree;
			varName = (ident.name.toString());
		} else {
			annotationNode.addError("invalid assignment" + tree + " : must be an identifier");
			return;
		}
		if (varName == null) {
			annotationNode.addError("assignement is null . Must be an identifier");
			return;
		}
		
		for (JavacNode f : annotatedVariables) {
			handleVarDeclaration(f, annotationInstance, parentNode, ident);
		}
		//		parentNode.rebuild();
	}

	private void handleVarDeclaration(JavacNode varNode, Onstruct annotationInstance, JavacNode parentNode, JCIdent ident) {
		String varName = OnstructUtils.varName(varNode.getName(), annotationInstance);
		String methName = OnstructUtils.methodName(varNode.getName(), annotationInstance);
		JCTree elem = varNode.get();
		JavacTreeMaker maker = varNode.getTreeMaker();
		//		System.out.println("create : var " + varName + " = " + ident.name + "." + methName + "();");
		if (elem instanceof JCVariableDecl) {
			JCVariableDecl variable = (JCVariableDecl) elem;
			variable.type = JavacHandlerUtil.chainDots(varNode, "lombok", "var").type;
			JCExpression methCall = maker.Select(ident, varNode.toName(methName));
			variable.init = maker.Apply(com.sun.tools.javac.util.List.<JCExpression>nil(),
				methCall,
				com.sun.tools.javac.util.List.<JCExpression>nil());
			variable.name = varNode.toName(varName);
			System.out.println("replaced with " + variable);
		} else
			System.err.println(varNode.get());
	}

}
