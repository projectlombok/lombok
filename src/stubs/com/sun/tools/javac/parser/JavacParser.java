/*
 * These are stub versions of various bits of javac-internal API (for various different versions of javac). Lombok is compiled against these.
 */
package com.sun.tools.javac.parser;

import com.sun.tools.javac.tree.JCTree;

public class JavacParser {
	// Up to javac26 the last boolean is keepEndPositions; as of javac27 (JDK-8372948) it is parseModuleInfo.
	// Javac -9(?) has: 
//	protected JavacParser(ParserFactory fac, Lexer S, boolean keepDocComments, boolean keepLineMap, boolean keepEndPositions)
	// Javac 27+ has:
//	protected JavacParser(ParserFactory fac, Lexer S, boolean keepDocComments, boolean keepLineMap, boolean parseModuleInfo)
	// These signatures are identical.
	
	protected JavacParser(ParserFactory fac, Lexer S, boolean keepDocComments, boolean keepLineMap, boolean varied) {
	}
	
	// Javac 10(?)-26.
	protected JavacParser(ParserFactory fac, Lexer S, boolean keepDocComments, boolean keepLineMap, boolean keepEndPositions, boolean parseModuleInfo) {
	}
	
	public JCTree.JCCompilationUnit parseCompilationUnit() {
		return null;
	}
}
