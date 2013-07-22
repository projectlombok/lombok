/*
 * Copyright (C) 2009-2011 The Project Lombok Authors.
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
package lombok.javac;

public final class CommentInfo {
	public enum StartConnection {
		/* Comment's start immediately follows a newline. */
		START_OF_LINE,
		
		/* Comment's start does not immediately follow a newline, but there is a newline between this
		 * and the previous comment (or pos 0 if first comment). */
		ON_NEXT_LINE,
		
		/* Comment's start immediately follows previous comment's end  (or pos 0 if first comment). */
		DIRECT_AFTER_PREVIOUS,
		
		/* Comment's start does not immediately follow previous comment's end, but there is no newline in
		 * between this and previous comment (or pos 0 if first comment). */
		AFTER_PREVIOUS
	}

	public enum EndConnection {
		/* Comment is followed immediately by another node (not whitespace, not newline). */
		DIRECT_AFTER_COMMENT,
		
		/* Comment is followed by some non-newline whitespace then by another node. */
		AFTER_COMMENT,
		
		/* Comment is followed by optionally some whitespace then a newline. */
		ON_NEXT_LINE
	}
	
	public final int pos;
	public final int prevEndPos;
	public final String content;
	public final int endPos;
	public final StartConnection start;
	public final EndConnection end;
	
	public CommentInfo(int prevEndPos, int pos, int endPos, String content, StartConnection start, EndConnection end) {
		this.pos = pos;
		this.prevEndPos = prevEndPos;
		this.endPos = endPos;
		this.content = content;
		this.start = start;
		this.end = end;
	}
	
	public boolean isJavadoc() {
		return content.startsWith("/**") && content.length() > 4;
	}
	
	@Override
	public String toString() {
		return String.format("%d: %s (%s,%s)", pos, content, start, end);
	}
}
