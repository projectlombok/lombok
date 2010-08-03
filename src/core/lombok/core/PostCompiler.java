/*
 * Copyright © 2010 Reinier Zwitserloot and Roel Spilker.
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
package lombok.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class PostCompiler {
	private PostCompiler() {/* prevent instantiation*/};
	
	private static List<PostCompilerTransformation> transformations;
	
	public static byte[] applyTransformations(byte[] original, String className, DiagnosticsReceiver diagnostics) {
		init(diagnostics);
		byte[] previous = original;
		for (PostCompilerTransformation transformation : transformations) {
			try {
				byte[] next = transformation.applyTransformations(previous, className, diagnostics);
				if (next != null) {
					previous = next;
				}
			} catch (Exception e) {
				diagnostics.addWarning(String.format("Error during the transformation of '%s'; post-compiler '%s' caused an exception: %s", className, transformation.getClass().getName(), e.getMessage()));
			}
		}
		return previous;
	}
	
	private static synchronized void init(DiagnosticsReceiver diagnostics) {
		if (transformations != null) return;
		transformations = new ArrayList<PostCompilerTransformation>();
		try {
			Iterator<PostCompilerTransformation> discovered = SpiLoadUtil.findServices(PostCompilerTransformation.class).iterator();
			while (discovered.hasNext()) {
				try {
					transformations.add(discovered.next());
				} catch (Exception e) {
					diagnostics.addWarning("Error during loading post-compile transformers: " + e.getMessage());
				}
			}
		} catch (IOException e) {
			diagnostics.addWarning("Could not load post-compile transformers: " + e.getMessage());
		}
	}
}
