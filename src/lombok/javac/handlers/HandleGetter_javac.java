package lombok.javac.handlers;

import static lombok.javac.handlers.PKG.*;

import lombok.Getter;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacAST;

import org.mangosdk.spi.ProviderFor;

import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

@ProviderFor(JavacAnnotationHandler.class)
public class HandleGetter_javac implements JavacAnnotationHandler<Getter> {
	@Override public void handle(AnnotationValues<Getter> annotation, JCAnnotation ast, JavacAST.Node node) {
		if ( node.up().getKind() != Kind.FIELD ) {
			node.addError("@Getter is only supported on a field.");
			return;
		}
		
		Getter getter = annotation.getInstance();
		
		JCClassDecl javacClassTree = (JCClassDecl) node.up().up().get();
		
		int access = toJavacModifier(getter.value());
		
		MethodTree getterMethod = createGetter(access, node.up(), node.getTreeMaker());
		javacClassTree.defs = javacClassTree.defs.append((JCTree)getterMethod);
	}
	
	private MethodTree createGetter(int access, JavacAST.Node field, TreeMaker treeMaker) {
		JCVariableDecl fieldNode = (JCVariableDecl) field.get();
		JCStatement returnStatement = treeMaker.Return(treeMaker.Ident(fieldNode.getName()));
		
		JCBlock methodBody = treeMaker.Block(0, List.of(returnStatement));
		Name methodName = field.toName(toGetterName((JCVariableDecl)field.get()));
		JCExpression methodType = fieldNode.type != null ? treeMaker.Type(fieldNode.type) : fieldNode.vartype;
		
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.nil();
		List<JCExpression> throwsClauses = List.nil();
		JCExpression annotationMethodDefaultValue = null;
		
		return treeMaker.MethodDef(treeMaker.Modifiers(access, List.<JCAnnotation>nil()), methodName, methodType,
				methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue);
	}
}
