/*
 * Copyright (C) 2015 The Project Lombok Authors.
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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotation that can be used to create {@code Singleton} types that
 * ensure only one instance of themselves are created.
 * 
 * <p>
 * The implementation comes in two flavors depending on the need for the type
 * to be instantiated:
 * <ul>
 *   <li>eagerly (default)</li>
 *   <li>lazily (on-demand)</li>
 * </ul>
 * 
 * @author mystarrocks
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface Singleton {
  /** 
   * The instantiation style to use.
   * 
   * <p>
   * Defaults to {@code EAGER}.
   */
  Style style() default Style.EAGER;

  /**
   * The instantiation style for the Singleton type.
   * 
   * @author mystarrocks
   */
  public static enum Style {
    /**
     * <pre>
     * public class MySingletonClass {
     *  private static final MySingletonClass INSTANCE = new MySingletonClass();
     * 
     *  public static MySingletonClass getInstance() {
     *    return INSTANCE;
     *  }
     * }
     * </pre>
     */
    EAGER,
    
    /**
     * <pre>
     * public class MySingletonClass {
     * 
     *  private static class MySingletonClassHolder {
     *    private static final MySingletonClass INSTANCE = new MySingletonClass();
     *  }
     * 
     *  public static MySingletonClass getInstance() {
     *    return MySingletonClassHolder.INSTANCE;
     *  }
     * }
     * </pre>
     */
    LAZY;
  }
}