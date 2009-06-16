package lombok.javac.handlers;

import static lombok.javac.handlers.PKG.*;

import javax.lang.model.element.Element;

import lombok.Getter;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

import org.mangosdk.spi.ProviderFor;

import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
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
	@Override public void handle(JavacNode node, Getter getter) {
		if ( !node.getJavacAST().getKind().isField() ) {
			node.addError("@Getter is only supported on a field.");
			return;
		}
		
		JCClassDecl javacClassTree = node.getEnclosingType();
		
		int access = toJavacModifier(getter.value());
		
		MethodTree getterMethod = createGetter(access, node.getJavacAST(), node.createTreeMaker(), node.createNameTable());
		javacClassTree.defs = javacClassTree.defs.append((JCTree)getterMethod);
	}
	
	private MethodTree createGetter(int access, Element field, TreeMaker treeMaker, Name.Table nameTable) {
		JCStatement returnStatement = treeMaker.Return(treeMaker.Ident((Symbol)field));
		
		JCBlock methodBody = treeMaker.Block(0, List.of(returnStatement));
		Name methodName = Name.fromString(nameTable, toGetterName(field));
		JCExpression methodType = treeMaker.Type((Type)field.asType());
		
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.nil();
		List<JCExpression> throwsClauses = List.nil();
		JCExpression annotationMethodDefaultValue = null;
		
		return treeMaker.MethodDef(treeMaker.Modifiers(access, List.<JCAnnotation>nil()), methodName, methodType,
				methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue);
	}
}
