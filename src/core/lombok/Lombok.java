/*
 * Copyright (C) 2009-2017 The Project Lombok Authors.
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

/**
 * Useful utility methods to manipulate lombok-generated code.
 */
public class Lombok {
	/**
	 * Throws any throwable 'sneakily' - you don't need to catch it, nor declare that you throw it onwards.
	 * The exception is still thrown - javac will just stop whining about it.
	 * <p>
	 * Example usage:
	 * <pre>public void run() {
	 *     throw sneakyThrow(new IOException("You don't need to catch me!"));
	 * }</pre>
	 * <p>
	 * NB: The exception is not wrapped, ignored, swallowed, or redefined. The JVM actually does not know or care
	 * about the concept of a 'checked exception'. All this method does is hide the act of throwing a checked exception
	 * from the java compiler.
	 * <p>
	 * Note that this method has a return type of {@code RuntimeException}; it is advised you always call this
	 * method as argument to the {@code throw} statement to avoid compiler errors regarding no return
	 * statement and similar problems. This method won't of course return an actual {@code RuntimeException} -
	 * it never returns, it always throws the provided exception.
	 * 
	 * @param t The throwable to throw without requiring you to catch its type.
	 * @return A dummy RuntimeException; this method never returns normally, it <em>always</em> throws an exception!
	 */
	public static RuntimeException sneakyThrow(Throwable t) {
		if (t == null) throw new NullPointerException("t");
		Lombok.<RuntimeException>sneakyThrow0(t);
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
		throw (T)t;
	}
	
	/**
	 * Returns the parameter directly.
	 * 
	 * This method can be used to prevent a static analyzer to determine the nullness of the passed parameter.
	 * 
	 * @param <T> the type of the parameter.
	 * @param value the value to return.
	 * @return value (this method just returns the parameter).
	 */
	public static <T> T preventNullAnalysis(T value) {
		return value;
	}
	
	/**
	 * Ensures that the {@code value} is not {@code null}.
	 * 
	 * @param <T> Type of the parameter.
	 * @param value the value to test for null.
	 * @param message the message of the {@link NullPointerException}.
	 * @return the value if it is not null.
	 * @throws NullPointerException with the {@code message} if the value is null.
	 */
	public static <T> T checkNotNull(T value, String message) {
		if (value == null) throw new NullPointerException(message);
		return value;
	}
}
