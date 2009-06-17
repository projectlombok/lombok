package lombok.javac.handlers;

import static lombok.javac.handlers.PKG.*;

import lombok.Getter;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacAST;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

@ProviderFor(JavacAnnotationHandler.class)
public class HandleGetter implements JavacAnnotationHandler<Getter> {
	@Override public boolean handle(AnnotationValues<Getter> annotation, JCAnnotation ast, JavacAST.Node annotationNode) {
		if ( annotationNode.up().getKind() != Kind.FIELD ) {
			annotationNode.addError("@Getter is only supported on a field.");
			return false;
		}
		
		String methodName = toGetterName((JCVariableDecl) annotationNode.up().get());
		
		if ( methodExists(methodName, annotationNode.up()) ) {
			annotationNode.addWarning(
					String.format("Not generating %s(): A method with that name already exists",  methodName));
			return false;
		}
		
		Getter getter = annotation.getInstance();
		
		JCClassDecl javacClassTree = (JCClassDecl) annotationNode.up().up().get();
		
		int access = toJavacModifier(getter.value());
		
		JCMethodDecl getterMethod = createGetter(access, annotationNode.up(), annotationNode.getTreeMaker());
		javacClassTree.defs = javacClassTree.defs.append(getterMethod);
		return true;
	}
	
	private JCMethodDecl createGetter(int access, JavacAST.Node field, TreeMaker treeMaker) {
		JCVariableDecl fieldNode = (JCVariableDecl) field.get();
		JCStatement returnStatement = treeMaker.Return(treeMaker.Ident(fieldNode.getName()));
		
		JCBlock methodBody = treeMaker.Block(0, List.of(returnStatement));
		Name methodName = field.toName(toGetterName(fieldNode));
		JCExpression methodType = fieldNode.type != null ? treeMaker.Type(fieldNode.type) : fieldNode.vartype;
		
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.nil();
		List<JCExpression> throwsClauses = List.nil();
		JCExpression annotationMethodDefaultValue = null;
		
		return treeMaker.MethodDef(treeMaker.Modifiers(access, List.<JCAnnotation>nil()), methodName, methodType,
				methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue);
	}
}
