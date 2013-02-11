/*
 * These are stub versions of various bits of javac-internal API (for various different versions of javac). Lombok is compiled against these.
 */
package com.sun.tools.javac.parser;

import com.sun.tools.javac.util.Context;

public class Parser {
	public static class Factory {
		public static Context.Key<Parser.Factory> parserFactoryKey;
		
		public Factory(Context context) {
			
		}
		
		public Parser newParser(Lexer S, boolean keepDocComments, boolean genEndPos) {
			return null;
		}
		
	}
}
