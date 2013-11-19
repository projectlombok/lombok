package com.sun.tools.javac.parser;

import java.nio.CharBuffer;

public class UnicodeReader {
	protected int bp;
	
	protected UnicodeReader(ScannerFactory sf, char[] input, int inputLength) {
	}
	
	protected UnicodeReader(ScannerFactory sf, CharBuffer buffer) {
		
	}
	
	public char[] getRawCharacters(int beginIndex, int endIndex) {
		return null;
	}
}
