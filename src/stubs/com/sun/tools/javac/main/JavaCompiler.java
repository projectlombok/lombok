/*
 * These are stub versions of various bits of javac-internal API (for various different versions of javac). Lombok is compiled against these.
 */
package com.sun.tools.javac.main;

import java.io.IOException;
import java.util.Collection;
import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;

import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.comp.Todo;

public class JavaCompiler {
	// Shared by JDK6-9
	public boolean keepComments;
	public boolean genEndPos;
	public Todo todo;
	
	public JavaCompiler(Context context) {}
	public int errorCount() { return 0; }
	public static String version() { return "<stub>"; }
	public JCCompilationUnit parse(String fileName) throws IOException { return null; }
	public List<JCCompilationUnit> enterTrees(List<JCCompilationUnit> roots) {return null;}
	
	//JDK up to 8
	public void initProcessAnnotations(Iterable<? extends Processor> processors) throws IOException {}
	public JavaCompiler processAnnotations(List<JCCompilationUnit> roots, List<String> classnames) {return this;}
	
	// JDK 9
	public void initProcessAnnotations(Iterable<? extends Processor> processors, Collection<? extends JavaFileObject> initialFiles, Collection<String> initialClassNames) {}
	public void processAnnotations(List<JCCompilationUnit> roots, Collection<String> classnames) {}
	public void close() {}
	public List<JCCompilationUnit> initModules(List<JCCompilationUnit> roots) { return null; }
}
