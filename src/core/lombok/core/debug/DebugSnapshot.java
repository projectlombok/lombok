/*
 * Copyright (C) 2012-2014 The Project Lombok Authors.
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

package lombok.core.debug;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;

public class DebugSnapshot implements Comparable<DebugSnapshot> {
	private static AtomicLong counter = new AtomicLong();
	
	private final long when, id = counter.getAndIncrement();
	private final long bits;
	private final List<StackTraceElement> trace;
	private final String threadName;
	private final String message;
	private final Object[] params;
	private final WeakReference<CompilationUnitDeclaration> owner;
	
	public DebugSnapshot(CompilationUnitDeclaration owner, int stackHiding, String message, Object... params) {
		this.when = System.currentTimeMillis();
		this.bits = owner.bits;
		if (stackHiding < 0) {
			this.trace = null;
		} else {
			StackTraceElement[] stackTrace = new Throwable().getStackTrace();
			this.trace = new ArrayList<StackTraceElement>(Math.max(0, stackTrace.length - stackHiding - 1));
			for (int i = 1 + stackHiding; i < stackTrace.length; i++) trace.add(stackTrace[i]);
		}
		this.threadName = Thread.currentThread().getName();
		this.message = message;
		this.params = params == null ? new Object[0] : params;
		this.owner = new WeakReference<CompilationUnitDeclaration>(owner);
	}
	
	private String ownerName() {
		CompilationUnitDeclaration node = owner.get();
		if (node == null) return "--GCed--";
		char[] tn = node.getMainTypeName();
		char[] fs = node.getFileName();
		if (tn == null || tn.length == 0) {
			return (fs == null || fs.length == 0) ? "--UNKNOWN--" : new String(fs);
		}
		
		return new String(tn);
	}
	
	public String shortToString() {
		StringBuilder out = new StringBuilder();
		out.append(String.format("WHEN: %14d THREAD: %s AST: %s HAMB: %b -- ", when, threadName, ownerName(), 0 != (bits & ASTNode.HasAllMethodBodies)));
		if (message != null) out.append(" ").append(String.format(message, params));
		return out.toString();
	}
	
	@Override public String toString() {
		StringBuilder out = new StringBuilder();
		out.append(shortToString()).append("\n");
		if (trace == null) {
			out.append("    Stack Omitted");
		} else {
			for (StackTraceElement elem : trace) {
				out.append("    ").append(elem.toString()).append("\n");
			}
		}
		return out.toString();
	}
	
	@Override public int compareTo(DebugSnapshot o) {
		return Long.valueOf(id).compareTo(o.id);
	}
}
