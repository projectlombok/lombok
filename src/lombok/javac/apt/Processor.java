package lombok.javac.apt;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import lombok.javac.HandlerLibrary;
import lombok.javac.JavacAST;
import lombok.javac.JavacASTAdapter;
import lombok.javac.JavacAST.Node;

import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;


@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Processor extends AbstractProcessor {
	private JavacProcessingEnvironment processingEnv;
	private HandlerLibrary handlers;
	private Trees trees;
	
	@Override public void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		if ( !(processingEnv instanceof JavacProcessingEnvironment) ) this.processingEnv = null;
		else {
			this.processingEnv = (JavacProcessingEnvironment) processingEnv;
			handlers = HandlerLibrary.load(processingEnv.getMessager());
			trees = Trees.instance(processingEnv);
		}
	}
	
	@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if ( processingEnv == null ) return false;
		
		
		IdentityHashMap<JCCompilationUnit, Void> units = new IdentityHashMap<JCCompilationUnit, Void>();
		for ( Element element : roundEnv.getRootElements() ) units.put(toUnit(element), null);
		
		List<JavacAST> asts = new ArrayList<JavacAST>();
		
		for ( JCCompilationUnit unit : units.keySet() ) asts.add(new JavacAST(trees, processingEnv, unit));
		
		for ( JavacAST ast : asts ) {
			ast.traverse(new AnnotationVisitor());
			handlers.callASTVisitors(ast);
		}
		return false;
	}
	
	private class AnnotationVisitor extends JavacASTAdapter {
		@Override public void visitAnnotationOnType(JCClassDecl type, Node annotationNode, JCAnnotation annotation) {
			if ( annotationNode.isHandled() ) return;
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			boolean handled = handlers.handleAnnotation(top, annotationNode, annotation);
			if ( handled ) annotationNode.setHandled();
		}
		
		@Override public void visitAnnotationOnField(JCVariableDecl field, Node annotationNode, JCAnnotation annotation) {
			if ( annotationNode.isHandled() ) return;
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			boolean handled = handlers.handleAnnotation(top, annotationNode, annotation);
			if ( handled ) annotationNode.setHandled();
		}
		
		@Override public void visitAnnotationOnMethod(JCMethodDecl method, Node annotationNode, JCAnnotation annotation) {
			if ( annotationNode.isHandled() ) return;
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			boolean handled = handlers.handleAnnotation(top, annotationNode, annotation);
			if ( handled ) annotationNode.setHandled();
		}
		
		@Override public void visitAnnotationOnMethodArgument(JCVariableDecl argument, JCMethodDecl method, Node annotationNode, JCAnnotation annotation) {
			if ( annotationNode.isHandled() ) return;
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			boolean handled = handlers.handleAnnotation(top, annotationNode, annotation);
			if ( handled ) annotationNode.setHandled();
		}
		
		@Override public void visitAnnotationOnLocal(JCVariableDecl local, Node annotationNode, JCAnnotation annotation) {
			if ( annotationNode.isHandled() ) return;
			JCCompilationUnit top = (JCCompilationUnit) annotationNode.top().get();
			boolean handled = handlers.handleAnnotation(top, annotationNode, annotation);
			if ( handled ) annotationNode.setHandled();
		}
	}
	
	private JCCompilationUnit toUnit(Element element) {
		return (JCCompilationUnit) trees.getPath(element).getCompilationUnit();
	}
}
