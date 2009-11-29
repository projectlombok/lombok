package lombok.netbeans.agent;

import java.util.Collections;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

import lombok.javac.JavacTransformer;

import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;

public class NetbeansEntryPoint implements TaskListener {
	public class DummyMessager implements Messager {
		@Override public void printMessage(Kind kind, CharSequence msg) {
			System.err.printf("%s: %s\n", kind, msg);
		}
		
		@Override public void printMessage(Kind kind, CharSequence msg, Element e) {
			printMessage(kind, msg);
		}
		
		@Override public void printMessage(Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
			printMessage(kind, msg);
		}
		
		@Override public void printMessage(Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v) {
			printMessage(kind, msg);
		}
	}
	
	private final Context context;
	
	public NetbeansEntryPoint(Context context) {
		this.context = context;
	}
	
	@Override public void started(TaskEvent event) {
		//we run at the end, so all the action is in #finished.
	}
	
	@Override public void finished(TaskEvent event) {
		if (TaskEvent.Kind.PARSE == event.getKind()) {
			JavacTransformer transformer = new JavacTransformer(new DummyMessager());	//TODO hook into netbeans error reporting!
			transformer.transform(context, Collections.singleton((JCCompilationUnit)event.getCompilationUnit()));
		}
	}
}
