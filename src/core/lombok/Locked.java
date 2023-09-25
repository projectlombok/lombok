/*
 * Copyright (C) 2021-2023 The Project Lombok Authors.
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
 * Guards all statements in an annotation method with a {@link java.util.concurrent.locks.Lock}.
 * <p>
 * For non-static methods, a field named {@code $lock} is used, and for static methods,
 * {@code $LOCK} is used. These will be generated if needed and if they aren't already present.
 * <p>
 * Because {@link Locked} uses a different type of lock from {@link Locked.Read} and {@link Locked.Write}, using both in
 * the same class using the default names will result in a compile time error.
 * <p>
 * Complete documentation is found at <a href="https://projectlombok.org/features/Locked">the project lombok features page for &#64;Locked</a>.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Locked {
	/**
	 * Locks using a {@link java.util.concurrent.locks.ReadWriteLock#readLock()}.
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.SOURCE)
	public @interface Read {
		/**
		 * Optional: specify the name of a different field to lock on. It is a compile time error if this field
		 * doesn't already exist (the fields are automatically generated only if you don't specify a specific name).
		 *
		 * @return Name of the field to lock on (blank = generate one).
		 */
		String value() default "";
	}
	
	/**
	 * Locks using a {@link java.util.concurrent.locks.ReadWriteLock#writeLock()}.
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.SOURCE)
	public @interface Write {
		/**
		 * Optional: specify the name of a different field to lock on. It is a compile time error if this field
		 * doesn't already exist (the fields are automatically generated only if you don't specify a specific name).
		 *
		 * @return Name of the field to lock on (blank = generate one).
		 */
		String value() default "";
	}
	
	/**
	 * Optional: specify the name of a different field to lock on. It is a compile time error if this field
	 * doesn't already exist (the fields are automatically generated only if you don't specify a specific name).
	 *
	 * @return Name of the field to lock on (blank = generate one).
	 */
	String value() default "";
}
