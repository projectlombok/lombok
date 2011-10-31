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
package lombok.javac.java7;

import java.nio.CharBuffer;

import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.util.Context;

public class CommentCollectingScannerFactory extends ScannerFactory {
	
	public static void preRegister(final Context context) {
		if (context.get(scannerFactoryKey) == null) {
			context.put(scannerFactoryKey, new Context.Factory<ScannerFactory>() {
				public ScannerFactory make() {
					return new CommentCollectingScannerFactory(context);
				}
				
				@Override public ScannerFactory make(Context c) {
					return new CommentCollectingScannerFactory(c);
				}
			});
		}
	}
	
	/** Create a new scanner factory. */
	protected CommentCollectingScannerFactory(Context context) {
		super(context);
	}
	
	@Override
	public Scanner newScanner(CharSequence input, boolean keepDocComments) {
		if (input instanceof CharBuffer) {
			return new CommentCollectingScanner(this, (CharBuffer)input);
		}
		char[] array = input.toString().toCharArray();
		return newScanner(array, array.length, keepDocComments);
	}
	
	@Override
	public Scanner newScanner(char[] input, int inputLength, boolean keepDocComments) {
		return new CommentCollectingScanner(this, input, inputLength);
	}
}
