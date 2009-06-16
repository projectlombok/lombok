package lombok.javac;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.util.Name;

public class JavacNode {
	private final Element node;
	private final Messager messager;
	private final JavacProcessingEnvironment env;
	private final Trees trees;
	
	public JavacNode(Trees trees, JavacProcessingEnvironment env, Element node) {
		this.trees = trees;
		this.env = env;
		this.node = node;
		this.messager = env.getMessager();
	}
	
	public Element getJavacAST() {
		return node;
	}
	
	public JCClassDecl getEnclosingType() {
		Element parent = node;
		while ( !(parent instanceof TypeElement) ) parent = node.getEnclosingElement();
		TypeElement classElement = (TypeElement)parent;
		return (JCClassDecl)trees.getTree(classElement);
	}
	
	public Name.Table createNameTable() {
		return Name.Table.instance(env.getContext());
	}
	
	public TreeMaker createTreeMaker() {
		return TreeMaker.instance(env.getContext());
	}
	
	public void addError(String message) {
		this.messager.printMessage(Diagnostic.Kind.ERROR, message, node);
	}
	
	public void addError(String message, AnnotationMirror mirror, AnnotationValue value) {
		this.messager.printMessage(Diagnostic.Kind.ERROR, message, node, mirror, value);
	}
	
	public void addWarning(String message) {
		this.messager.printMessage(Diagnostic.Kind.WARNING, message, node);
	}
	
	public void addWarning(String message, AnnotationMirror mirror, AnnotationValue value) {
		this.messager.printMessage(Diagnostic.Kind.WARNING, message, node, mirror, value);
	}
}
