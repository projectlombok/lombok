package lombok.javac.java6;

import java.util.Map;

import lombok.javac.CommentInfo;

import com.sun.tools.javac.parser.EndPosParser;
import com.sun.tools.javac.parser.Lexer;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.List;

class CommentCollectingParser extends EndPosParser {
	
	private final Map<JCCompilationUnit, List<CommentInfo>> commentsMap;
	private final Lexer lexer;
	
	protected CommentCollectingParser(Parser.Factory fac, Lexer S, boolean keepDocComments, Map<JCCompilationUnit, List<CommentInfo>> commentsMap) {
		super(fac, S, keepDocComments);
		lexer = S;
		this.commentsMap = commentsMap;
	}
	
	@Override public JCCompilationUnit compilationUnit() {
		JCCompilationUnit result = super.compilationUnit();
		if (lexer instanceof CommentCollectingScanner) {
			List<CommentInfo> comments = ((CommentCollectingScanner)lexer).getComments();
			commentsMap.put(result, comments);
		}
		return result;
	}
}