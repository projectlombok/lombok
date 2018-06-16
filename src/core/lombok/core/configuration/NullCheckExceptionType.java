/*
 * Copyright (C) 2014-2018 The Project Lombok Authors.
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


@ExampleValueString("[NullPointerException | IllegalArgumentException]")
public enum NullCheckExceptionType {
	ILLEGAL_ARGUMENT_EXCEPTION {
		public String toExceptionMessage(String fieldName) {
			return fieldName + " is marked @NonNull but is null";
		}
		
		@Override public String getExceptionType() {
			return "java.lang.IllegalArgumentException";
		}
	},
	NULL_POINTER_EXCEPTION {
		@Override public String toExceptionMessage(String fieldName) {
			return fieldName + " is marked @NonNull but is null";
		}
		
		public String getExceptionType() {
			return "java.lang.NullPointerException";
		}
	};
	
	public abstract String toExceptionMessage(String fieldName);
	public abstract String getExceptionType();
}
