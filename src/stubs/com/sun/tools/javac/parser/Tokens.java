package com.sun.tools.javac.parser;

public class Tokens {
	public static class Token {
		public int pos;
	}
	
	public interface Comment {
		enum CommentStyle {
			LINE, BLOCK, JAVADOC,
		}
		
		String getText();
		
		int getSourcePos(int index);
		
		CommentStyle getStyle();
		
		boolean isDeprecated();
	}
}
