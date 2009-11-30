/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
