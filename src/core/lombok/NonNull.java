/*
 * Copyright (C) 2009-2019 The Project Lombok Authors.
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If put on a parameter, lombok will insert a null-check at the start of the method / constructor's body, throwing a
 * {@code NullPointerException} with the parameter's name as message. If put on a field, any generated method assigning
 * a value to this field will also produce these null-checks.
 * <p>
 * This is a type-use annotation: only an annotation on the outermost type triggers a null-check.
 * For example, {@code String @NonNull []} marks the array reference as non-null, whereas
 * {@code @NonNull String[]} marks its element type and {@code String[] @NonNull []} marks its
 * inner array references. Neither element annotation triggers a check of the outer array reference.
 * The same distinction applies to varargs: {@code String @NonNull ...} checks the array reference,
 * whereas {@code @NonNull String...} only annotates the element type.
 * Annotations on type arguments do not trigger null-checks of the enclosing reference either.
 */
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface NonNull {
}
