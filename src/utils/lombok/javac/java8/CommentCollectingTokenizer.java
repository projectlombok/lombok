/*
 * Copyright (C) 2013-2020 The Project Lombok Authors.
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
package lombok.javac.java8;

import java.nio.CharBuffer;

import lombok.javac.CommentInfo;
import lombok.javac.CommentInfo.EndConnection;
import lombok.javac.CommentInfo.StartConnection;

import com.sun.tools.javac.parser.JavaTokenizer;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.parser.Tokens.Token;
import com.sun.tools.javac.parser.Tokens.Comment.CommentStyle;
import com.sun.tools.javac.parser.UnicodeReader;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

class CommentCollectingTokenizer extends JavaTokenizer {
	private int prevEndPosition = 0;
	private final ListBuffer<CommentInfo> comments = new ListBuffer<CommentInfo>();
	private final ListBuffer<Integer> textBlockStarts;
	private int endComment = 0;
	
	CommentCollectingTokenizer(ScannerFactory fac, char[] buf, int inputLength, boolean findTextBlocks) {
		super(fac, new PositionUnicodeReader(fac, buf, inputLength));
		textBlockStarts = findTextBlocks ? new ListBuffer<Integer>() : null;
	}

	CommentCollectingTokenizer(ScannerFactory fac, CharBuffer buf, boolean findTextBlocks) {
		super(fac, new PositionUnicodeReader(fac, buf));
		textBlockStarts = findTextBlocks ? new ListBuffer<Integer>() : null;
	}
	
	int pos() {
		return ((PositionUnicodeReader) reader).pos();
	}
	
	@Override public Token readToken() {
		Token token = super.readToken();
		prevEndPosition = pos();
		if (textBlockStarts != null && (prevEndPosition - token.pos > 5) && token.getClass().getName().endsWith("$StringToken")) {
			char[] start = reader.getRawCharacters(token.pos, token.pos + 3);
			if (start[0] == '"' && start[1] == '"' && start[2] == '"') textBlockStarts.add(token.pos);
		}
		return token;
	}
	
	@Override
	protected Comment processComment(int pos, int endPos, CommentStyle style) {
		int prevEndPos = Math.max(prevEndPosition, endComment);
		endComment = endPos;
		String content = new String(reader.getRawCharacters(pos, endPos));
		StartConnection start = determineStartConnection(prevEndPos, pos);
		EndConnection end = determineEndConnection(endPos); 

		CommentInfo comment = new CommentInfo(prevEndPos, pos, endPos, content, start, end);
		comments.append(comment);

		return super.processComment(pos, endPos, style);
	}
	
	private EndConnection determineEndConnection(int pos) {
		boolean first = true;
		for (int i = pos;; i++) {
			char c;
			try {
				c = reader.getRawCharacters(i, i + 1)[0];
			} catch (IndexOutOfBoundsException e) {
				c = '\n';
			}
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
		char[] between = reader.getRawCharacters(from, to);
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
	
	public List<CommentInfo> getComments() {
		return comments.toList();
	}	
	
	public List<Integer> getTextBlockStarts() {
		return textBlockStarts == null ? List.<Integer>nil() : textBlockStarts.toList();
	}
	
	static class PositionUnicodeReader extends UnicodeReader {
		protected PositionUnicodeReader(ScannerFactory sf, char[] input, int inputLength) {
			super(sf, input, inputLength);
		}
		
		public PositionUnicodeReader(ScannerFactory sf, CharBuffer buffer) {
			super(sf, buffer);
		}
		
		int pos() {
			return bp;
		}
	}
}