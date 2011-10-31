/*
 * These are stub versions of various bits of javac-internal API (for various different versions of javac). Lombok is compiled against these.
 */
package com.sun.tools.javac.parser;

import com.sun.tools.javac.util.Context;

public class ScannerFactory {
	public static final Context.Key<ScannerFactory> scannerFactoryKey = null;
	
	protected ScannerFactory(Context c) {
	}
	
	public static ScannerFactory instance(Context c) {
		return null;
	}
	
	public Scanner newScanner(CharSequence input, boolean keepDocComments) {
		return null;
	}
	
	public Scanner newScanner(char[] input, int inputLength, boolean keepDocComments) {
		return null;
	}
}
