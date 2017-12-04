/*
 * These are stub versions of various bits of javac-internal API (for various different versions of javac). Lombok is compiled against these.
 */
package com.sun.tools.javac.parser;

import com.sun.tools.javac.tree.JCTree;

public class JavacParser {
	protected JavacParser(ParserFactory fac, Lexer S, boolean keepDocComments, boolean keepLineMap, boolean keepEndPositions) {
	}
	
	protected JavacParser(ParserFactory fac, Lexer S, boolean keepDocComments, boolean keepLineMap, boolean keepEndPositions, boolean parseModuleInfo) {
	}
	
	public JCTree.JCCompilationUnit parseCompilationUnit() {
		return null;
	}
}
