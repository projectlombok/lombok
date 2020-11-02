/*
 * Copyright (C) 2020 The Project Lombok Authors.
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
package lombok.extern.javafx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Put on any field to make lombok build getter and setter for a JavaFx property.
 * <p>
 * Complete documentation is found at <a href="https://projectlombok.org/features/experimental/FxProperty">the project lombok features page for &#64;FxProperty</a>.
 * <p>
 * Example:
 * <pre>
 *     private &#64;FxProperty StringProperty foo;
 * </pre>
 * 
 * will generate:
 * 
 * <pre>
 *     public final String getFoo() {
 *         return this.foo.get();
 *     }
 *     public final void setFoo(String foo) {
 *         return this.foo.set(foo);
 *     }
 *     public StringProperty fooProperty() {
 *         return this.foo;
 *     }
 * </pre>
 * <p>
 * This annotation can also be applied to a class, in which case it'll be as if all fields that don't already have
 * a {@code @FxProperty} annotation.
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface FxProperty {
	/**
	 * Generate a read only property instead of normal one. This will create a
	 * private setter and converts the return type of the property accessor to
	 * the right ReadOnly*Property. 
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * 	private &#64;FxProperty(readOnly = true) StringProperty foo;
	 * </pre>
	 * 
	 * will generate:
	 * 
	 * <pre>
	 * 	public final String getFoo() {
	 * 		return this.foo.get();
	 * 	}
	 * 
	 * 	private final void setFoo(String foo) {
	 * 		return this.foo.set(foo);
	 * 	}
	 * 
	 * 	public ReadOnlyStringProperty fooProperty() {
	 * 		return this.foo;
	 * 	}
	 * </pre>
	 * 
	 * @return
	 */
	boolean readOnly() default false;
}
