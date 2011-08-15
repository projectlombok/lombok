/*
 * Copyright © 2011 Reinier Zwitserloot and Roel Spilker.
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
package lombok.delombok.java6;

import java.nio.CharBuffer;

import lombok.delombok.Delombok.Comments;

import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.util.Context;

public class CommentCollectingScannerFactory extends Scanner.Factory {
	private final Context context;
	
	public static void preRegister(final Context context) {
		context.put(scannerFactoryKey, new Context.Factory<Scanner.Factory>() {
			public CommentCollectingScanner.Factory make() {
				return new CommentCollectingScannerFactory(context);
			}
			
			public CommentCollectingScanner.Factory make(Context c) {
				return new CommentCollectingScannerFactory(c);
			}
		});
	}
	
	/** Create a new scanner factory. */
	protected CommentCollectingScannerFactory(Context context) {
		super(context);
		this.context = context;
	}
	
	@Override
	public Scanner newScanner(CharSequence input) {
		if (input instanceof CharBuffer) {
			return new CommentCollectingScanner(this, (CharBuffer)input, context.get(Comments.class));
		}
		char[] array = input.toString().toCharArray();
		return newScanner(array, array.length);
	}
	
	@Override
	public Scanner newScanner(char[] input, int inputLength) {
		return new CommentCollectingScanner(this, input, inputLength, context.get(Comments.class));
	}
}