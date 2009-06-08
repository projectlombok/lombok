package lombok.apt;

import java.lang.reflect.Modifier;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import lombok.Getter;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
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

class HandleGetter_javac extends HandlerForCompiler<Getter> {
	private final Trees trees;
	private final Messager messager;
	private JavacProcessingEnvironment env;
	
	HandleGetter_javac() {
		this.messager = processEnv.getMessager();
		this.trees = Trees.instance(processEnv);
	}
	
	@Override public void init() {
		this.env = (JavacProcessingEnvironment)processEnv;
	}
	
	@Override public void handle(Element element, Getter getter) {
		if ( !element.getKind().isField() ) {
			messager.printMessage(Diagnostic.Kind.ERROR, "@Getter is only supported on a field.");
			return;
		}
		
		TypeElement classElement = (TypeElement)element.getEnclosingElement();
		JCClassDecl javacClassTree = (JCClassDecl)trees.getTree(classElement);
		
		Name.Table nameTable = Name.Table.instance(env.getContext());
		TreeMaker treeMaker = TreeMaker.instance(env.getContext());
		
		MethodTree getterMethod = createGetter(element, treeMaker, nameTable);
		javacClassTree.defs = javacClassTree.defs.append((JCTree)getterMethod);
	}
	
	private MethodTree createGetter(Element field, TreeMaker treeMaker, Name.Table nameTable) {
		JCStatement returnStatement = treeMaker.Return(treeMaker.Ident((Symbol)field));
		
		//TODO Trab the position in the source file of the field by looking it up in the JCClassDecl,
		//and copy it into the 'position' info for the Ident and Return AST Nodes.
		
		JCBlock methodBody = treeMaker.Block(0, List.of(returnStatement));
		Name methodName = Name.fromString(nameTable, PKG.toGetterName(field));
		JCExpression methodType = treeMaker.Type((Type)field.asType());
		
		List<JCTypeParameter> methodGenericParams = List.nil();
		List<JCVariableDecl> parameters = List.nil();
		List<JCExpression> throwsClauses = List.nil();
		JCExpression annotationMethodDefaultValue = null;
		
		return treeMaker.MethodDef(treeMaker.Modifiers(Modifier.PUBLIC, List.<JCAnnotation>nil()), methodName, methodType,
				methodGenericParams, parameters, throwsClauses, methodBody, annotationMethodDefaultValue);
	}
}
