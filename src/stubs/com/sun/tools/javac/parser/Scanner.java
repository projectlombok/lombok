/*
 * These are stub versions of various bits of javac-internal API (for various different versions of javac). Lombok is compiled against these.
 */
package com.sun.tools.javac.parser;

import java.nio.CharBuffer;

import com.sun.tools.javac.util.Context;

public class Scanner implements Lexer {
	protected Scanner(Factory fac, CharBuffer buffer) {
	}
	
	protected Scanner(Factory fac, char[] input, int inputLength) {
	}
	
	protected Scanner(ScannerFactory fac, CharBuffer buffer) {
	}
	
	protected Scanner(ScannerFactory fac, char[] input, int inputLength) {
	}
	
	protected Scanner(ScannerFactory fac, JavaTokenizer tokenizer) {
	}
	
	public static class Factory {
		public static final Context.Key<Scanner.Factory> scannerFactoryKey = null;
		
		protected Factory(Context context) {
		}
		
		public Scanner newScanner(CharSequence input) {
			return null;
		}
		
		public Scanner newScanner(char[] input, int inputLength) {
			return null;
		}
	}
	
	public enum CommentStyle {
		LINE,
		BLOCK,
		JAVADOC,
	}
	
	protected void processComment(CommentStyle style) {
	}
	
	public int prevEndPos() {
		return -1;
	}
	
	public int endPos() {
		return -1;
	}
	
	public int pos() {
		return -1;
	}
	
	public char[] getRawCharacters(int beginIndex, int endIndex) {
		return null;
	}
	
	public void nextToken() {
	}
	
	public char[] getRawCharacters() {
		return new char[0];
	}
}
