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
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

class CommentCollectingTokenizer extends JavaTokenizer {
	private int prevEndPosition = 0;
	private final ListBuffer<CommentInfo> comments = new ListBuffer<CommentInfo>();
	private int endComment = 0;

	CommentCollectingTokenizer(ScannerFactory fac, char[] buf, int inputLength) {
		super(fac, new PositionUnicodeReader(fac, buf, inputLength));
	}

	CommentCollectingTokenizer(ScannerFactory fac, CharBuffer buf) {
		super(fac, new PositionUnicodeReader(fac, buf));
	}
	
	@Override public Token readToken() {
		Token token = super.readToken();
		prevEndPosition = ((PositionUnicodeReader)reader).pos();
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