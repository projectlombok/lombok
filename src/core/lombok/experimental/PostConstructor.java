/*
 * Copyright (C) 2022 The Project Lombok Authors.
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
 * Put on any method to make lombok add a call to the annotated method in all constructors.
 * <p>
 * Every explicit constructor requires either a {@code this(...)} statement,
 * {@link InvokePostConstructors @InvokePostConstructors} or {@link SkipPostConstructors @SkipPostConstructors}.
 * <p>
 * Complete documentation is found at <a href="https://projectlombok.org/features/experimental/PostConstructor">the project lombok features page for &#64;PostConstructor</a>.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface PostConstructor {
	/**
	 * If present lombok modifies this constructor to call all methods annotated with
	 * {@link PostConstructor @PostConstructor}.
	 * <p>
	 * If this constructor contains a {@code return} statement lombok wraps all statements with a {@code try {...} catch
	 * {...} finally {...}} block otherwise the calls are appended at the end
	 */
	@Target({ElementType.CONSTRUCTOR})
	@Retention(RetentionPolicy.SOURCE)
	public @interface InvokePostConstructors {
		
	}
	
	/**
	 * If present lombok will not modify this constructor and will not call methods annotated with
	 * {@link PostConstructor @PostConstructor}.
	 */
	@Target({ElementType.CONSTRUCTOR})
	@Retention(RetentionPolicy.SOURCE)
	public @interface SkipPostConstructors {
		
	}
}
