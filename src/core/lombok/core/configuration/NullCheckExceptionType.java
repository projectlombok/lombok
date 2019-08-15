/*
 * Copyright (C) 2014-2019 The Project Lombok Authors.
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
package lombok.core.configuration;

import lombok.core.LombokImmutableList;

@ExampleValueString("[NullPointerException | IllegalArgumentException | Assertion | JDK | GUAVA]")
public enum NullCheckExceptionType {
	ILLEGAL_ARGUMENT_EXCEPTION {
		@Override public String getExceptionType() {
			return "java.lang.IllegalArgumentException";
		}

		@Override public LombokImmutableList<String> getMethod() {
			return null;
		}
	},
	NULL_POINTER_EXCEPTION {
		@Override public String getExceptionType() {
			return "java.lang.NullPointerException";
		}

		@Override public LombokImmutableList<String> getMethod() {
			return null;
		}
	},
	ASSERTION {
		@Override public String getExceptionType() {
			return null;
		}

		@Override public LombokImmutableList<String> getMethod() {
			return null;
		}
	},
	JDK {
		@Override public String getExceptionType() {
			return null;
		}

		@Override public LombokImmutableList<String> getMethod() {
			return METHOD_JDK;
		}
	},
	GUAVA {
		@Override public String getExceptionType() {
			return null;
		}

		@Override public LombokImmutableList<String> getMethod() {
			return METHOD_GUAVA;
		}
	};
	
	private static final LombokImmutableList<String> METHOD_JDK = LombokImmutableList.of("java", "util", "Objects", "requireNonNull");
	private static final LombokImmutableList<String> METHOD_GUAVA = LombokImmutableList.of("com", "google", "common", "base", "Preconditions", "checkNotNull");
	
	public String toExceptionMessage(String fieldName) {
		return fieldName + " is marked non-null but is null";
	}
	
	public abstract String getExceptionType();
	
	public abstract LombokImmutableList<String> getMethod();
}
