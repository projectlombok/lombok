/*
 * Copyright © 2011 Reinier Zwitserloot, Roel Spilker and Robbert Jan Grootjans.
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
package lombok.javac;

import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;

public class Comments {
	public void register(Context context) {
		try {
			if (JavaCompiler.version().startsWith("1.6")) {
				Class.forName("lombok.javac.java6.CommentCollectingScannerFactory").getMethod("preRegister", Context.class).invoke(null, context);
			} else {
				Class.forName("lombok.javac.java7.CommentCollectingScannerFactory").getMethod("preRegister", Context.class).invoke(null, context);
			}
		} catch (Exception e) {
			if (e instanceof RuntimeException) throw (RuntimeException)e;
			throw new RuntimeException(e);
		}
		context.put(Comments.class, (Comments) null);
		context.put(Comments.class, this);
	}
	
	private com.sun.tools.javac.util.ListBuffer<Comment> comments = com.sun.tools.javac.util.ListBuffer.lb();
	
	public com.sun.tools.javac.util.ListBuffer<Comment> getComments() {
		return comments;
	}
	
	public void add(Comment comment) {
		comments.append(comment);
	}
}
