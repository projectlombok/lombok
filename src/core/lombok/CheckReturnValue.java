/*
 * Copyright (C) 2025 The Project Lombok Authors.
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
 * Lombok adds this annotation to generated methods where the return value should not be ignored.
 * <p>
 * For example, {@code @With} methods return a new instance, so ignoring the return value is always a bug.
 * Similarly, {@code @Builder}'s {@code build()} method produces the built object.
 * <p>
 * Static analysis tools (Error Prone, IntelliJ, SpotBugs) recognize {@code @CheckReturnValue}
 * by simple class name, regardless of package, and will warn when the return value is discarded.
 * <p>
 * If you want to opt out, you can add {@code lombok.addCheckReturnValueAnnotation = false} to
 * {@code lombok.config}.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface CheckReturnValue {
}
