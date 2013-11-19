package com.sun.tools.javac.parser;

import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.parser.Tokens.Comment.CommentStyle;

public class JavaTokenizer {
	protected UnicodeReader reader;
	
	protected JavaTokenizer(ScannerFactory fac, UnicodeReader reader) {
	}
	
	public com.sun.tools.javac.parser.Tokens.Token readToken() {
		return null;
	}

	protected Comment processComment(int pos, int endPos, CommentStyle style) {
		return null;
	}
}
