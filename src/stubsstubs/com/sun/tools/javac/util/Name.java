package com.sun.tools.javac.util;

public class Name implements javax.lang.model.element.Name {
	public boolean contentEquals(CharSequence cs) { return false; }
	public int length() { return 0; }
	public char charAt(int idx) { return '\0'; }
	public CharSequence subSequence(int a, int b) { return null; }
}