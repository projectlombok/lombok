/*
 * These are stub versions of various bits of javac-internal API (for various different versions of javac). Lombok is compiled against these.
 */
package com.sun.tools.javadoc;

import java.nio.CharBuffer;

import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.util.Context;

public class DocCommentScanner extends Scanner {
	protected DocCommentScanner(com.sun.tools.javadoc.DocCommentScanner.Factory fac, CharBuffer buffer) {
		super(fac, buffer);
	}
	
	protected DocCommentScanner(com.sun.tools.javadoc.DocCommentScanner.Factory fac, char[] input, int inputLength) {
		super(fac, input, inputLength);
	}
	
	public static class Factory extends Scanner.Factory {
		protected Factory(Context context) {
			super(context);
		}
	}
}
