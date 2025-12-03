package com.sun.tools.javac.parser;

import com.sun.tools.javac.util.JCDiagnostic;

public class Tokens {
	public static class Token {
		public int pos;
	}
	
	public interface Comment {
		enum CommentStyle {
			LINE, BLOCK, JAVADOC,
		}
		
		String getText();
		
		Comment stripIndent();
		
		JCDiagnostic.DiagnosticPosition getPos();
		
		int getSourcePos(int index);
		
		CommentStyle getStyle();
		
		boolean isDeprecated();
	}
}
