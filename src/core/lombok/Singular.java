/*
 * Copyright (C) 2015-2017 The Project Lombok Authors.
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

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import lombok.core.LombokImmutableList;
import lombok.core.configuration.ExampleValueString;
import lombok.core.configuration.NullCheckExceptionType;

/**
 * The singular annotation is used together with {@code @Builder} to create single element 'add' methods in the builder for collections.
 */
@Target({FIELD, PARAMETER})
@Retention(SOURCE)
public @interface Singular {
	/** @return The singular name of this field. If it's a normal english plural, lombok will figure it out automatically. Otherwise, this parameter is mandatory. */
	String value() default "";
	
	NullCollectionBehavior nullBehavior() default NullCollectionBehavior.NULL_POINTER_EXCEPTION;
	
	@ExampleValueString("[NullPointerException | IllegalArgumentException | JDK | Guava | Ignore]")
	public enum NullCollectionBehavior {
		ILLEGAL_ARGUMENT_EXCEPTION {
			@Override public String getExceptionType() {
				return NullCheckExceptionType.ILLEGAL_ARGUMENT_EXCEPTION.getExceptionType();
			}
			
			@Override public LombokImmutableList<String> getMethod() {
				return NullCheckExceptionType.ILLEGAL_ARGUMENT_EXCEPTION.getMethod();
			}
		},
		NULL_POINTER_EXCEPTION {
			@Override public String getExceptionType() {
				return NullCheckExceptionType.NULL_POINTER_EXCEPTION.getExceptionType();
			}
			
			@Override public LombokImmutableList<String> getMethod() {
				return NullCheckExceptionType.NULL_POINTER_EXCEPTION.getMethod();
			}
		},
		JDK {
			@Override public String getExceptionType() {
				return NullCheckExceptionType.JDK.getExceptionType();
			}
			
			@Override public LombokImmutableList<String> getMethod() {
				return NullCheckExceptionType.JDK.getMethod();
			}
		},
		GUAVA {
			@Override public String getExceptionType() {
				return NullCheckExceptionType.GUAVA.getExceptionType();
			}
			
			@Override public LombokImmutableList<String> getMethod() {
				return NullCheckExceptionType.GUAVA.getMethod();
			}
		},
		IGNORE {
			@Override public String getExceptionType() {
				return null;
			}
			
			@Override public LombokImmutableList<String> getMethod() {
				return null;
			}
		};
		
		
		public String toExceptionMessage(String fieldName) {
			return fieldName + " cannot be null";
		}
		
		public abstract String getExceptionType();
		
		public abstract LombokImmutableList<String> getMethod();
	}
}
