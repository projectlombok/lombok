package lombok.javac.java6;

import java.lang.reflect.Field;
import java.util.Map;

import lombok.javac.CommentInfo;

import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.parser.Lexer;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

public class CommentCollectingParserFactory extends Parser.Factory {
	private final Map<JCCompilationUnit, List<CommentInfo>> commentsMap;
	
	static Context.Key<Parser.Factory> key() {
		return parserFactoryKey;
	}
	
	protected CommentCollectingParserFactory(Context context, Map<JCCompilationUnit, List<CommentInfo>> commentsMap) {
		super(context);
		this.commentsMap = commentsMap;
	}
	
	@Override public Parser newParser(Lexer S, boolean keepDocComments, boolean genEndPos) {
		Object x = new CommentCollectingParser(this, S, true, commentsMap);
		return (Parser) x;
		// CCP is based on a stub which extends nothing, but at runtime the stub is replaced with either
		//javac6's EndPosParser which extends Parser, or javac7's EndPosParser which implements Parser.
		//Either way this will work out.
	}
	
	public static void setInCompiler(JavaCompiler compiler, Context context, Map<JCCompilationUnit, List<CommentInfo>> commentsMap) {
		context.put(CommentCollectingParserFactory.key(), (Parser.Factory)null);
		Field field;
		try {
			field = JavaCompiler.class.getDeclaredField("parserFactory");
			field.setAccessible(true);
			field.set(compiler, new CommentCollectingParserFactory(context, commentsMap));
		} catch (Exception e) {
			throw new IllegalStateException("Could not set comment sensitive parser in the compiler", e);
		}
	}
}