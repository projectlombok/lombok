/*
 * Copyright (C) 2012-2013 The Project Lombok Authors.
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
 * A container for settings for the generation of getters and setters.
 * <p>
 * Complete documentation is found at <a href="https://projectlombok.org/features/experimental/Accessors.html">the project lombok features page for &#64;Accessors</a>.
 * <p>
 * Using this annotation does nothing by itself; an annotation that makes lombok generate getters and setters,
 * such as {@link lombok.Setter} or {@link lombok.Data} is also required.
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface Accessors {
	/**
	 * If true, accessors will be named after the field and not include a <code>get</code> or <code>set</code>
	 * prefix. If true and <code>chain</code> is omitted, <code>chain</code> defaults to <code>true</code>.
	 * <strong>default: false</strong>
	 */
	boolean fluent() default false;
	
	/**
	 * If true, setters return <code>this</code> instead of <code>void</code>.
	 * <strong>default: false</strong>, unless <code>fluent=true</code>, then <strong>default: true</code>
	 */
	boolean chain() default false;
	
	/**
	 * If present, only fields with any of the stated prefixes are given the getter/setter treatment.
	 * Note that a prefix only counts if the next character is NOT a lowercase character or the last
	 * letter of the prefix is not a letter (for instance an underscore). If multiple fields
	 * all turn into the same name when the prefix is stripped, an error will be generated.
	 */
	String[] prefix() default {};
	
	/**
	 * If true, a propertyName constant is generated (e. g. 'public final String
	 * PROP_FOO = "foo";' for the property foo).
	 */
	boolean propertyNameConstant() default false;
	
	/**
	 * If true, property change support is added to the setter implementation.
	 * This will also cause the generation of propertyNameConstant(s).
	 */
	boolean bound() default false;
	
	/**
	 * field name to use for bound setters to call firePropertyChange on -
	 * instance type must be java.beans.PropertyChangeSupport.
	 */
	String propertyChangeSupportFieldName() default "propertySupport";
}
