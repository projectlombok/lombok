/*
 * These are stub versions of various bits of javac-internal API (for various different versions of javac). Lombok is compiled against these.
 */
package com.sun.tools.javac.parser;

import java.nio.CharBuffer;

public class DocCommentScanner extends Scanner {
	protected DocCommentScanner(Factory fac, CharBuffer buffer) {
		super(fac, buffer);
	}
	
	protected DocCommentScanner(Factory fac, char[] input, int inputLength) {
		super(fac, input, inputLength);
	}
	
	protected DocCommentScanner(ScannerFactory fac, CharBuffer buffer) {
		super(fac, buffer);
	}
	
	protected DocCommentScanner(ScannerFactory fac, char[] input, int inputLength) {
		super(fac, input, inputLength);
	}
	
}
