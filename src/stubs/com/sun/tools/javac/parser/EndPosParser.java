/*
 * These are stub versions of various bits of javac-internal API (for various different versions of javac). Lombok is compiled against these.
 */
package com.sun.tools.javac.parser;

import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

public class EndPosParser {
	public EndPosParser(Parser.Factory fac, Lexer S, boolean keepDocComments) {
	}
	
	public EndPosParser(ParserFactory fac, Lexer S, boolean keepDocComments, boolean keepLineMap) {
	}
	
	public JCCompilationUnit compilationUnit() {
		return null;
	}
	
	public JCCompilationUnit parseCompilationUnit() {
		return null;
	}
}
