/*
 * Copyright (C) 2013 The Project Lombok Authors.
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

import java.util.List;
import java.util.Map;

import lombok.javac.CommentInfo;

import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.Lexer;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

class CommentCollectingParser extends JavacParser {
	private final Map<JCCompilationUnit, List<CommentInfo>> commentsMap;
	private final Lexer lexer;
	
	protected CommentCollectingParser(ParserFactory fac, Lexer S,
			boolean keepDocComments, boolean keepLineMap, boolean keepEndPositions, Map<JCCompilationUnit, List<CommentInfo>> commentsMap) {
		super(fac, S, keepDocComments, keepLineMap, keepEndPositions);
		lexer = S;
		this.commentsMap = commentsMap;
	}
	
	public JCCompilationUnit parseCompilationUnit() {
		JCCompilationUnit result = super.parseCompilationUnit();
		if (lexer instanceof CommentCollectingScanner) {
			List<CommentInfo> comments = ((CommentCollectingScanner)lexer).getComments();
			commentsMap.put(result, comments);
		}
		return result;
	}
}