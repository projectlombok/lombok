package lombok.javac.java8;

import lombok.javac.CommentInfo;

import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.util.List;

public class CommentCollectingScanner extends Scanner {
	
	private CommentCollectingTokenizer tokenizer;
	
	public CommentCollectingScanner(ScannerFactory fac, CommentCollectingTokenizer tokenizer) {
		super(fac, tokenizer);
		this.tokenizer = tokenizer;
	}
	
	public List<CommentInfo> getComments() {
		return tokenizer.getComments();
	}
}