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
package lombok.delombok;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringWriter;
import java.nio.CharBuffer;

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.main.OptionName;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Options;

public class PrettyPrinter {

	private final Context context = new Context();
	private final JavaCompiler compiler;

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
//			System.out.println("Usage: java %s <file1> <file2>...");
			args = new String[] {"tests/foo/WithComments.java"};
		}
		PrettyPrinter prettyPrinter = new PrettyPrinter();
		for (String fileName : args) {
			prettyPrinter.print(fileName);
		}
	}

	public PrettyPrinter() {
		Options.instance(context).put(OptionName.ENCODING, "utf-8");
		PrettyDocCommentScanner.Factory.preRegister(context);
		compiler = new JavaCompiler(context) {
			@Override
			protected boolean keepComments() {
				return true;
			}
		};
		compiler.genEndPos = true;
	}
	
	public static class Comments {
//		private static Key<Comments> commentsKey = new Key<Comments>();
		
		private List<Comment> comments = List.nil();
		
		void add(int pos, String content) {
			comments = comments.append(new Comment(pos, content));
		}
	}
	
	private void print(String fileName) throws Exception {
		
		Comments comments = new Comments();
		context.put(Comments.class, comments);
		
		@SuppressWarnings("deprecation")
		final JCCompilationUnit cu = compiler.parse(fileName);
		
		StringWriter writer = new StringWriter();
		cu.accept(new PrettyCommentsPrinter(writer, comments.comments, cu));
		System.out.printf("####### Original source of %s #######\n", fileName);
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line = null;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		reader.close();
		
		System.out.printf("####### Generated source of %s #######\n", fileName);
		System.out.println(writer);
		
		printNodes(fileName, cu);
		System.out.printf("#######   Comments of %s  #######\n", fileName);
		for (Comment comment : comments.comments) {
			System.out.println(comment.summary());
		}
		System.out.printf("#######   End of %s  #######\n\n", fileName);
	}

	private void printNodes(String fileName, final JCCompilationUnit cu) {
		System.out.printf("#######   Nodes of %s  #######\n", fileName);
		cu.accept(new TreeScanner<Void, Void>(){
			@Override
			public Void scan(Tree node, Void p) {
				if (node == null) {
					return null;
				}
				node.accept(new SimpleTreeVisitor<Void, Void>(){
					@SuppressWarnings("incomplete-switch")
					@Override
					protected Void defaultAction(Tree treeNode, Void r) {
						if (treeNode == null) {
							return null;
						}
						JCTree tree = (JCTree)treeNode;
						Kind kind = tree.getKind();
						System.out.print(kind.toString() + " " + tree.pos + " - "  + tree.getEndPosition(cu.endPositions));
						switch (kind) {
						case IDENTIFIER:
						case PRIMITIVE_TYPE:
						case MODIFIERS:
							System.out.print("[" + tree.toString() + "]");
							break;
						}
						System.out.println();
						return null;
					}
				}, null);
				return super.scan(node, p);
			}
		}, null);
	}
	
	public static class PrettyDocCommentScanner extends Scanner {

	    private final Comments comments;

		/** A factory for creating scanners. */
	    public static class Factory extends Scanner.Factory {

	    	private final Context context;

			public static void preRegister(final Context context) {
			    context.put(scannerFactoryKey, new Context.Factory<Scanner.Factory>() {
					public Factory make() {
					    return new Factory(context);
					}
			    });
			}
		
			/** Create a new scanner factory. */
			protected Factory(Context context) {
			    super(context);
				this.context = context;
			}

	        @Override
	        public Scanner newScanner(CharSequence input) {
	            if (input instanceof CharBuffer) {
	            	return new PrettyDocCommentScanner(this, (CharBuffer)input, context.get(Comments.class));
	            } 
                char[] array = input.toString().toCharArray();
                return newScanner(array, array.length);
	        }
		
	        @Override
	        public Scanner newScanner(char[] input, int inputLength) {
	            return new PrettyDocCommentScanner(this, input, inputLength, context.get(Comments.class));
	        }
	    }
	    
		
		public PrettyDocCommentScanner(PrettyPrinter.PrettyDocCommentScanner.Factory factory, CharBuffer charBuffer, Comments comments) {
			super(factory, charBuffer);
			this.comments = comments;
		}


		public PrettyDocCommentScanner(PrettyPrinter.PrettyDocCommentScanner.Factory factory, char[] input, int inputLength, Comments comments) {
			super(factory, input, inputLength);
			this.comments = comments;
		}

		@Override
		protected void processComment(CommentStyle style) {
			comments.add(pos(), new String(getRawCharacters(pos(), endPos())));
		}
	}
}
