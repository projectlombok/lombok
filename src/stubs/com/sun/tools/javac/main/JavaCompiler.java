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
	/* JDK [6,)   */ public boolean keepComments;
	/* JDK [6,26] */ public boolean genEndPos; // JDK27+: Removed; end pos are stored on nodes themselves
	/* JDK [6,)   */ public Todo todo;
	
	/* JDK [6,)   */ public JavaCompiler(Context context) {}
	/* JDK [6,)   */ public int errorCount() { return 0; }
	/* JDK [6,)   */ public static String version() { return "<stub>"; }
	/* JDK [6,)   */ public JCCompilationUnit parse(String fileName) throws IOException { return null; }
	/* JDK [6,)   */ public List<JCCompilationUnit> enterTrees(List<JCCompilationUnit> roots) {return null;}
	
	/* JDK [6,8]  */ public void initProcessAnnotations(Iterable<? extends Processor> processors) throws IOException {}
	/* JDK [9,)   */ public void initProcessAnnotations(Iterable<? extends Processor> processors, Collection<? extends JavaFileObject> initialFiles, Collection<String> initialClassNames) {}
	/* JDK [6,8]  */ public JavaCompiler processAnnotations(List<JCCompilationUnit> roots, List<String> classnames) {return this;}
	/* JDK [9,)   */ public void processAnnotations(List<JCCompilationUnit> roots, Collection<String> classnames) {}
	/* JDK [9,)   */ public void close() {}
	/* JDK [9,)   */ public List<JCCompilationUnit> initModules(List<JCCompilationUnit> roots) { return null; }
}
