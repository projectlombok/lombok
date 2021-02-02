/*
 * Copyright (C) 2011-2021 The Project Lombok Authors.
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

import java.nio.Buffer;
import java.nio.CharBuffer;

import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.util.Context;

public class CommentCollectingScannerFactory extends ScannerFactory {
	public static boolean findTextBlocks;
	@SuppressWarnings("all")
	public static void preRegister(final Context context) {
		if (context.get(scannerFactoryKey) == null) {
			// Careful! There is voodoo magic here!
			//
			// Context.Factory is parameterized. make() is for javac6 and below; make(Context) is for javac7 and up.
			// this anonymous inner class definition is intentionally 'raw' - the return type of both 'make' methods is 'T',
			// which means the compiler will only generate the correct "real" override method (with returntype Object, which is
			// the lower bound for T, as a synthetic accessor for the make with returntype ScannerFactory) for that make method which
			// is actually on the classpath (either make() for javac6-, or make(Context) for javac7+).
			//
			// We normally solve this issue via src/stubs, with BOTH make methods listed, but for some reason the presence of a stubbed out
			// Context (or even a complete copy, it doesn't matter) results in a really strange eclipse bug, where any mention of any kind
			// of com.sun.tools.javac.tree.TreeMaker in a source file disables ALL usage of 'go to declaration' and auto-complete in the entire
			// source file.
			//
			// Thus, in short:
			// * Do NOT parameterize the anonymous inner class literal.
			// * Leave the return types as 'j.l.Object'.
			// * Leave both make methods intact; deleting one has no effect on javac6- / javac7+, but breaks the other. Hard to test for.
			// * Do not stub com.sun.tools.javac.util.Context or any of its inner types, like Factory.
			@SuppressWarnings("all")
			class MyFactory implements Context.Factory {
				// This overrides the javac6- version of make.
				public Object make() {
					return new CommentCollectingScannerFactory(context);
				}
				
				// This overrides the javac7+ version.
				public Object make(Context c) {
					return new CommentCollectingScannerFactory(c);
				}
			}
			@SuppressWarnings("unchecked") Context.Factory<ScannerFactory> factory = new MyFactory();
			context.put(scannerFactoryKey, factory);
		}
	}
	
	/** Create a new scanner factory. */
	protected CommentCollectingScannerFactory(Context context) {
		super(context);
	}
	
	@SuppressWarnings("all")
	@Override
	public Scanner newScanner(CharSequence input, boolean keepDocComments) {
		char[] array;
		int limit;
		if (input instanceof CharBuffer && ((CharBuffer) input).hasArray()) {
			CharBuffer cb = (CharBuffer) input;
			((Buffer)cb.compact()).flip();
			array = cb.array();
			limit = cb.limit();
		} else {
			array = input.toString().toCharArray();
			limit = array.length;
		}
		if (array.length == limit) {
			// work around a bug where the last comment in a file falls away in this case.
			char[] d = new char[limit + 1];
			System.arraycopy(array, 0, d, 0, limit);
			array = d;
		}
		return newScanner(array, limit, keepDocComments);
	}
	
	@Override
	public Scanner newScanner(char[] input, int inputLength, boolean keepDocComments) {
		return new CommentCollectingScanner(this, CommentCollectingTokenizer.create(this, input, inputLength, findTextBlocks));
	}
}
