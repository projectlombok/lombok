/*
 * Copyright (C) 2009-2014 The Project Lombok Authors.
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
 * Generates implementations for the {@code equals} and {@code hashCode} methods inherited by all objects, based on relevant fields.
 * <p>
 * Complete documentation is found at <a href="http://projectlombok.org/features/EqualsAndHashCode.html">the project lombok features page for &#64;EqualsAndHashCode</a>.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface EqualsAndHashCode {
	/**
	 * Any fields listed here will not be taken into account in the generated
	 * {@code equals} and {@code hashCode} implementations.
	 * Mutually exclusive with {@link #of()}.
	 */
	String[] exclude() default {};
	
	/**
	 * If present, explicitly lists the fields that are to be used for identity.
	 * Normally, all non-static, non-transient fields are used for identity.
	 * <p>
	 * Mutually exclusive with {@link #exclude()}.
	 */
	String[] of() default {};
	
	/**
	 * Call on the superclass's implementations of {@code equals} and {@code hashCode} before calculating
	 * for the fields in this class.
	 * <strong>default: false</strong>
	 */
	boolean callSuper() default false;
	
	/**
	 * Normally, if getters are available, those are called. To suppress this and let the generated code use the fields directly, set this to {@code true}.
	 * <strong>default: false</strong>
	 */
	boolean doNotUseGetters() default false;
	
	/**
	 * Any annotations listed here are put on the generated parameter of {@code equals} and {@code canEqual}. The syntax for this feature is: {@code @EqualsAndHashCode(onParam=@__({@AnnotationsGoHere}))}
	 * This is useful to add for example a {@code Nullable} annotation.
	 */
	AnyAnnotation[] onParam() default {};
	
	/**
	 * Placeholder annotation to enable the placement of annotations on the generated code.
	 * @deprecated Don't use this annotation, ever - Read the documentation.
	 */
	@Deprecated
	@Retention(RetentionPolicy.SOURCE)
	@Target({})
	@interface AnyAnnotation {}
}
