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
package lombok.javac;

public final class Comment {
	public enum StartConnection {
		START_OF_LINE,
		ON_NEXT_LINE,
		DIRECT_AFTER_PREVIOUS,
		AFTER_PREVIOUS
	}

	public enum EndConnection {
		DIRECT_AFTER_COMMENT,
		AFTER_COMMENT,
		ON_NEXT_LINE
	}
	
	public final int pos;
	public final int prevEndPos;
	public final String content;
	public final int endPos;
	public final StartConnection start;
	public final EndConnection end;
	
	public Comment(int prevEndPos, int pos, int endPos, String content, StartConnection start, EndConnection end) {
		this.pos = pos;
		this.prevEndPos = prevEndPos;
		this.endPos = endPos;
		this.content = content;
		this.start = start;
		this.end = end;
	}
	
	@Override
	public String toString() {
		return String.format("%d: %s (%s,%s)", pos, content, start, end);
	}
}
