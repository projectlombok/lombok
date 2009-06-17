package lombok.javac.handlers;

import static lombok.javac.handlers.PKG.*;
import lombok.Setter;
import lombok.core.AnnotationValues;
import lombok.core.AST.Kind;
import lombok.javac.JavacAST;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacAST.Node;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

@ProviderFor(JavacAnnotationHandler.class)
public class HandleSetter implements JavacAnnotationHandler<Setter> {
	@Override public boolean handle(AnnotationValues<Setter> annotation, JCAnnotation ast, Node annotationNode) {
		if ( annotationNode.up().getKind() != Kind.FIELD ) {
			annotationNode.addError("@Setter is only supported on a field.");
			return false;
		}
		
		JCVariableDecl fieldNode = (JCVariableDecl) annotationNode.up().get();
		String methodName = toSetterName(fieldNode);
		
		if ( methodExists(methodName, annotationNode.up()) ) {
			annotationNode.addWarning(
					String.format("Not generating %s(%s %s): A method with that name already exists",
							methodName, fieldNode.vartype, fieldNode.name));
			return false;
		}
		
		Setter setter = annotation.getInstance();
		
		JCClassDecl javacClassTree = (JCClassDecl) annotationNode.up().up().get();
		
		int access = toJavacModifier(setter.value());
		
		JCMethodDecl setterMethod = createSetter(access, annotationNode.up(), annotationNode.getTreeMaker());
		javacClassTree.defs = javacClassTree.defs.append(setterMethod);
		return true;
	}
	
	private JCMethodDecl createSetter(int access, JavacAST.Node field, TreeMaker treeMaker) {
		JCVariableDecl fieldDecl = (JCVariableDecl) field.get();
		
		JCFieldAccess thisX = treeMaker.Select(treeMaker.Ident(field.toName("this")), fieldDecl.name);
		JCAssign assign = treeMaker.Assign(thisX, treeMaker.Ident(fieldDecl.name));
		
		JCBlock methodBody = treeMaker.Block(0, List.<JCStatement>of(treeMaker.Exec(assign)));
		Name methodName = field.toName(toSetterName(fieldDecl));
		JCVariableDecl param = treeMaker.VarDef(treeMaker.Modifiers(0, List.<JCAnnotation>nil()), fieldDecl.name, fieldDecl.vartype, null);
		JCExpression methodType = treeMaker.Type(field.getSymbolTable().voidType);
		
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.of(param);
		List<JCExpression> throwsClauses = List.nil();
		JCExpression annotationMethodDefaultValue = null;
		
		return treeMaker.MethodDef(treeMaker.Modifiers(access, List.<JCAnnotation>nil()), methodName, methodType,
				methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue);
	}
}
