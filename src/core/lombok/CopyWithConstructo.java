/*
 * Copyright (C) 2010-2025 The Project Lombok Authors.
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
 * Generates a <em>copy-with</em> method for the annotated type.
 * <p>
 * A <em>copy-with</em> method creates a new instance of the class, copying all
 * fields from the original object while allowing selected fields to be replaced
 * with new values.
 *
 * <h2>Example</h2>
 * <pre>
 * {@code
 * @CopyWith
 * class Person {
 *     private final String name;
 *     private final int age;
 * }
 * }
 * </pre>
 *
 * Will generate (conceptually):
 * <pre>
 * {@code
 * class Person {
 *     private final String name;
 *     private final int age;
 *
 *     public Person copyWith(String name, int age) {
 *         return new Person(
 *             name != null ? name : this.name,
 *             age != 0 ? age : this.age
 *         );
 *     }
 * }
 * }
 * </pre>
 *
 * <p>
 * The access level of the generated method can be customized with {@link #access()}.
 *
 * <p>
 * Complete documentation is found at
 * <a href="https://projectlombok.org/features/copywith">the project lombok features page for &#64;CopyWith</a>.
 *
 * @see lombok.AllArgsConstructor
 * @see lombok.With
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CopyWith {
    /**
     * Sets the access level of the generated {@code copyWith(...)} method.
     * By default, the method is {@code public}.
     *
     * @return The access modifier of the generated method.
     */
    AccessLevel access() default AccessLevel.PUBLIC;
}
