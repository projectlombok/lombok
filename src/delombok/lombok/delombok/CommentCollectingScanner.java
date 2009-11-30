/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.delombok;

import java.nio.CharBuffer;

import lombok.delombok.Comment.EndConnection;
import lombok.delombok.Comment.StartConnection;
import lombok.delombok.CommentPreservingParser.Comments;

import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.util.Context;

public class CommentCollectingScanner extends Scanner {
	private final Comments comments;
	private int endComment = 0;

	/** A factory for creating scanners. */
	public static class Factory extends Scanner.Factory {
		private final Context context;
		
		public static void preRegister(final Context context) {
			context.put(scannerFactoryKey, new Context.Factory<Scanner.Factory>() {
				public CommentCollectingScanner.Factory make() {
					return new Factory(context);
				}
			});
		}
		
		/** Create a new scanner factory. */
		protected Factory(Context context) {
			super(context);
			this.context = context;
		}
		
		@Override
		public Scanner newScanner(CharSequence input) {
			if (input instanceof CharBuffer) {
				return new CommentCollectingScanner(this, (CharBuffer)input, context.get(Comments.class));
			}
			char[] array = input.toString().toCharArray();
			return newScanner(array, array.length);
		}
		
		@Override
		public Scanner newScanner(char[] input, int inputLength) {
			return new CommentCollectingScanner(this, input, inputLength, context.get(Comments.class));
		}
	}
	
	
	public CommentCollectingScanner(CommentCollectingScanner.Factory factory, CharBuffer charBuffer, Comments comments) {
		super(factory, charBuffer);
		this.comments = comments;
	}
	
	public CommentCollectingScanner(CommentCollectingScanner.Factory factory, char[] input, int inputLength, Comments comments) {
		super(factory, input, inputLength);
		this.comments = comments;
	}
	
	@Override
	protected void processComment(CommentStyle style) {
		int prevEndPos = Math.max(prevEndPos(), endComment);
		int pos = pos();
		int endPos = endPos();
		endComment = endPos;
		String content = new String(getRawCharacters(pos, endPos));
		StartConnection start = determineStartConnection(prevEndPos, pos);
		EndConnection end = determineEndConnection(endPos); 

		Comment comment = new Comment(prevEndPos, pos, endPos, content, start, end);
		comments.add(comment);
	}
	
	private EndConnection determineEndConnection(int pos) {
		boolean first = true;
		for (int i = pos;; i++) {
			char c = getRawCharacters(i, i + 1)[0];
			if (isNewLine(c)) {
				return EndConnection.ON_NEXT_LINE;
			}
			if (Character.isWhitespace(c)) {
				first = false;
				continue;
			}
			return first ? EndConnection.DIRECT_AFTER_COMMENT : EndConnection.AFTER_COMMENT;
		}
	}

	private StartConnection determineStartConnection(int from, int to) {
		if (from == to) {
			return StartConnection.DIRECT_AFTER_PREVIOUS;
		}
		char[] between = getRawCharacters(from, to);
		if (isNewLine(between[between.length - 1])) {
			return StartConnection.START_OF_LINE;
		}
		for (char c : between) {
			if (isNewLine(c)) {
				return StartConnection.ON_NEXT_LINE;
			}
		}
		return StartConnection.AFTER_PREVIOUS;
	}

	private boolean isNewLine(char c) {
		return c == '\n' || c == '\r';
	}
}
