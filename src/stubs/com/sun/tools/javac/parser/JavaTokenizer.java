package com.sun.tools.javac.parser;

import java.nio.CharBuffer;

import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.parser.Tokens.Comment.CommentStyle;

public class JavaTokenizer {
	// used before java 16
	protected UnicodeReader reader;
	
	// used before java 16
	protected JavaTokenizer(ScannerFactory fac, UnicodeReader reader) {
	}
	
	public com.sun.tools.javac.parser.Tokens.Token readToken() {
		return null;
	}

	protected Comment processComment(int pos, int endPos, CommentStyle style) {
		return null;
	}
	
	// used in java 16
	protected JavaTokenizer(ScannerFactory fac, char[] buf, int inputLength) {
	}
	
	// used in java 16
	protected JavaTokenizer(ScannerFactory fac, CharBuffer buf) {
	}

	// introduced in java 16
	protected int position() {
		return -1;
	}
}
