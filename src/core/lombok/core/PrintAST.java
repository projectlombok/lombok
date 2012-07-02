/*
 * Copyright (C) 2009-2012 The Project Lombok Authors.
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Will print the tree structure of annotated node and all its children.
 * 
 * This annotation is useful only for those working on Lombok, for example to test if a Lombok handlers is doing its
 * job correctly, or to see what the imagined endresult of a transformation is supposed to look like.
 */
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface PrintAST {
	/**
	 * Normally, the AST is printed to standard out, but you can pick a filename instead. Useful for many IDEs
	 * which don't have a console unless you start them from the command line.
	 */
	String outfile() default "";
	
	/**
	 * Sets whether to print node structure (false) or generated java code (true).
	 * 
	 * By setting printContent to true, the annotated element's java code representation is printed. If false,
	 * its node structure (e.g. node classname) is printed, and this process is repeated for all children.
	 */
	boolean printContent() default false;
	
	/**
	 * if {@code true} prints the start and end position of each node.
	 */
	boolean printPositions() default false;
}
