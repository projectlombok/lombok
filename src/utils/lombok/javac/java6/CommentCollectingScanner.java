/*
 * Copyright (C) 2011-2013 The Project Lombok Authors.
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
package lombok.javac.java6;

import java.nio.CharBuffer;

import lombok.javac.CommentInfo;
import lombok.javac.CommentInfo.EndConnection;
import lombok.javac.CommentInfo.StartConnection;

import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

public class CommentCollectingScanner extends Scanner {
	private final ListBuffer<CommentInfo> comments = new ListBuffer<CommentInfo>();
	private int endComment = 0;
	
	public CommentCollectingScanner(CommentCollectingScannerFactory factory, CharBuffer charBuffer) {
		super(factory, charBuffer);
	}
	
	public CommentCollectingScanner(CommentCollectingScannerFactory factory, char[] input, int inputLength) {
		super(factory, input, inputLength);
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

		CommentInfo comment = new CommentInfo(prevEndPos, pos, endPos, content, start, end);
		comments.append(comment);
		super.processComment(style);
	}
	
	private EndConnection determineEndConnection(int pos) {
		boolean first = true;
		for (int i = pos;; i++) {
			char c;
			try {
				c = getRawCharacters(i, i + 1)[0];
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

	public List<CommentInfo> getComments() {
		return comments.toList();
	}
}
