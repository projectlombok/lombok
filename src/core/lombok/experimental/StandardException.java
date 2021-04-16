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

import lombok.AccessLevel;

/**
 * Put on any class that extends some {@code java.lang.Throwable} type to add the 4 common exception constructors.
 * 
 * Specifically, all 4 constructors derived from the combinatorial explosion of {@code String message} and {@code Throwable cause}.
 * You may write any or all of these 4 constructors by hand; lombok will only generate the missing ones.
 * <p>
 * All but the full {@code (String message, Throwable cause)} constructor are implemented as a {@code this(msg, cause)} call; it is therefore
 * possibly to write code to run on construction by writing just the {@code (String message, Throwable cause)} constructor.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface StandardException {
	/**
	 * Sets the access level of the generated constuctors. By default, generated constructors are {@code public}.
	 * Note: This does nothing if you write your own constructors (we won't change their access levels).
	 * 
	 * @return The constructors will be generated with this access modifier.
	 */
	AccessLevel access() default lombok.AccessLevel.PUBLIC;
}
