/*
 * Copyright (C) 2021 The Project Lombok Authors.
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
package lombok.experimental;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Put on any class that implements some interfaces but does not intend to implement all of the methods.
 * Lombok is going to generate a default implementation for all the non-implemented methods.</p>
 * 
 * <p>The default behavior is that the generated methods would throw an {@link UnsupportedOperationException}.</p>
 * 
 * <p>When silent mode is used, then the generated methods would return a default value based upon the return type of the method.</p>
 * <ul>
 * <li>For primitive types and their object wrappers the return value is the default primitive value 
 * (e.g. <code>0</code> for an <code>int</code> or <code>Integer</code>, <code>false</code> for a <code>boolean</code> or <code>Boolean</code>, etc).</li>
 * <li>For maps and collections the return value is an empty map or collection (e.g. <code>Collections.emptyList()</code>)</li>
 * <li>For any other objects the return value is <code>null</code></li>
 * </ul>
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Adapter {
	/**
	 * List of interfaces to generate the methods for.
	 */
	Class<?>[] of() default {};

	/**
	 * If true, then the generated methods return default value (e.g. null) instead of throwing
	 * {@link UnsupportedOperationException}.
	 * 
	 * The <code>silent=false</code> can be customized using the {@link #throwException()} and {@link #message()} attributes.
	 */
	boolean silent() default false;

	/**
	 * If specified, then the generated methods are going to throw this type of
	 * exception.
	 * 
	 * Default value is {@link UnsupportedOperationException}
	 */
	Class<? extends RuntimeException> throwException() default UnsupportedOperationException.class;
	
	/**
	 * In conjunction with the {@link #throwException()} attribute, the throw
	 * exception's message will be this.
	 * 
	 * Default value is empty, so the exception won't have any message attribute set.
	 */
	String message() default "";
	
	/**
	 * If true, then the generated methods won't declare the overridden method's
	 * throws clause.
	 */
	boolean suppressThrows() default false;

}
