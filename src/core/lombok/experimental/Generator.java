/*
 * Copyright (C) 2023 The Project Lombok Authors.
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
 * The {@code Generator} annotation allows user to write iterator easily.
 * <p>
 * By adding {@code @Generator} to method, user can use {@code yieldThis} or {@code yieldAll}
 * to suspend execution and yield values to iterator.
 * <p>
 * The method's return type must be one of {@code java.lang.Iterable} or {@code java.util.Iterator}
 * as it will return iterator (or iterable returning self as iterator).
 * <p>
 * Example
 * <pre>
 * &#64;Generator
 * public Iterable&lt;Integer&gt; range(int from, int to) {
 *  for (int i = from; i < to; i++) {
 *      yieldThis(i);
 *  }
 * }
 * </pre>
 * Calling {@code range(0, 10)} will create iterator returning from 0 to 9.
 * <p>
 * Example code will be expanded like below
 * <pre>
 * public Iterable&lt;Integer&gt; range(int from, int to) {
 *  class __Generator extends lombok.Lombok.Generator&lt;Integer&gt; {
 *      protected void advance() {
 *          for (int i = from; i < to; i++) {
 *              yieldThis(i);
 *          }
 *      }
 *  }
 *  return new __Generator();
 * </pre>
 * Note: Class extending {@code lombok.Lombok.Generator} is transformed in bytecode level into state machine after compiled.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Generator {}
