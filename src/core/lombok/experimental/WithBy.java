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
package lombok.experimental;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import lombok.AccessLevel;

/**
 * Put on any field to make lombok build a 'withBy' - a withFieldNameBy method which produces a clone of this object (except for 1 field which gets a new value).
 * <p>
 * Complete documentation is found at <a href="https://projectlombok.org/features/experimental/WithBy">the project lombok features page for &#64;WithBy</a>.
 * <p>
 * Example:
 * <pre>
 *     private &#64;WithBy final int foo;
 *     private &#64;WithBy final String bar;
 * </pre>
 * 
 * will generate:
 * 
 * <pre>
 *     public SELF_TYPE withFooBy(@lombok.NonNull IntUnaryOperator operator) {
 *         int foo = operator.apply(this.foo);
 *         return this.foo == foo ? this : new SELF_TYPE(foo, bar);
 *     }
 *     public SELF_TYPE withBarBy(@lombok.NonNull Function&lt;? super String, ? extends String&gt; operator) {
 *         String bar = operator.apply(this.bar);
 *         return this.bar == bar ? this : new SELF_TYPE(foo, bar);
 *     }
 * </pre>
 * <p>
 * This annotation can also be applied to a class, in which case it'll be as if all non-static fields that don't already have
 * a {@code WithBy} annotation have the annotation.
 * <p>
 * This annotation is primarily useful for hierarchical immutable data structures. For example:
 * 
 * <pre>
 *     class Movie {
 *         &#64;WithBy private final Director director;
 *     }
 *     
 *     class Director {
 *         &#64;WithBy private final LocalDate birthDate;
 *     }
 * </pre>
 * 
 * Using plain old {@code @With}, to increment a movie's director's birth date by one, you would write:
 * 
 * <pre>
 *     movie = movie.withDirector(movie.getDirector().withBirthDate(movie.getDirector().getBirthDate().plusDays(1)));
 * </pre>
 * 
 * but with {@code @WithBy}, you'd write:
 * 
 * <pre>
 *     movie = movie.withDirectorBy(d -&gt; d.withBirthDateBy(bd -&gt; bd.plusDays(1)));
 * </pre>
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface WithBy {
	/**
	 * If you want your with method to be non-public, you can specify an alternate access level here.
	 * 
	 * @return The method will be generated with this access modifier.
	 */
	AccessLevel value() default AccessLevel.PUBLIC;
	
	/**
	 * Any annotations listed here are put on the generated method.
	 * The syntax for this feature depends on JDK version (nothing we can do about that; it's to work around javac bugs).<br>
	 * up to JDK7:<br>
	 *  {@code @With(onMethod=@__({@AnnotationsGoHere}))}<br>
	 * from JDK8:<br>
	 *  {@code @With(onMethod_={@AnnotationsGohere})} // note the underscore after {@code onMethod}.
	 * 
	 * @return List of annotations to apply to the generated method.
	 */
	AnyAnnotation[] onMethod() default {};
	
	/**
	  * Placeholder annotation to enable the placement of annotations on the generated code.
	  * @deprecated Don't use this annotation, ever - Read the documentation.
	  */
	@Deprecated
	@Retention(RetentionPolicy.SOURCE)
	@Target({})
	@interface AnyAnnotation {}
}
