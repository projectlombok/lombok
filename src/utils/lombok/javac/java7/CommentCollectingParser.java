package lombok.javac.java7;

import java.util.List;
import java.util.Map;

import lombok.javac.CommentInfo;

import com.sun.tools.javac.parser.EndPosParser;
import com.sun.tools.javac.parser.Lexer;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

class CommentCollectingParser extends EndPosParser {
	private final Map<JCCompilationUnit, List<CommentInfo>> commentsMap;
	private final Lexer lexer;
	
	protected CommentCollectingParser(ParserFactory fac, Lexer S,
			boolean keepDocComments, boolean keepLineMap, Map<JCCompilationUnit, List<CommentInfo>> commentsMap) {
		super(fac, S, keepDocComments, keepLineMap);
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