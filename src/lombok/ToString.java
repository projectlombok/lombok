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
package lombok;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates an implementation for the <code>toString</code> method inherited by all objects.
 * <p>
 * If the method already exists, then <code>&#64;ToString</code> will not generate any method, and instead warns
 * that it's doing nothing at all. The parameter list and return type are not relevant when deciding to skip generation of
 * the method; any method named <code>toString</code> will make <code>&#64;ToString</code> not generate anything.
 * <p>
 * All fields that are non-static are used in the toString generation. You can exclude fields by specifying them
 * in the <code>exclude</code> parameter.
 * <p>
 * Array fields are handled by way of {@link java.util.Arrays#deepToString(Object[])} where necessary.
 * The downside is that arrays with circular references (arrays that contain themselves,
 * possibly indirectly) results in calls to <code>toString</code> throwing a
 * {@link java.lang.StackOverflowError}. However, the implementations for java's own {@link java.util.ArrayList} suffer
 * from the same flaw.
 * <p>
 * The <code>toString</code> method that is generated will print the class name as well as each field. You can optionally
 * also print the names of each field, by setting the <code>includeFieldNames</code> flag to <em>true</em>.
 * <p>
 * You can also choose to include the result of <code>toString</code> in your class's superclass by setting the
 * <code>callSuper</code> to <em>true</em>.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface ToString {
	/**
	 * Include the name of each field when printing it.
	 * <strong>default: false</strong>
	 */
	boolean includeFieldNames() default false;
	
	/**
	 * Any fields listed here will not be printed in the generated <code>toString</code> implementation.
	 */
	String[] exclude() default {};
	
	/**
	 * Include the result of the superclass's implementation of <code>toString</code> in the output.
	 * <strong>default: false</strong>
	 */
	boolean callSuper() default false;
}
