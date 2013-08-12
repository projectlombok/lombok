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