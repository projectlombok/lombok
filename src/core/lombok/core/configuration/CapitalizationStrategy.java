/*
 * Copyright (C) 2021 The Project Lombok Authors.
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

/** Used for lombok configuration to determine how to transform field names when turning them into accessor method names and vice versa. */
public enum CapitalizationStrategy {
	BASIC {
		@Override public String capitalize(String in) {
			if (in.length() == 0) return in;
			char first = in.charAt(0);
			if (!Character.isLowerCase(first)) return in;
			boolean useUpperCase = in.length() > 2 &&
				(Character.isTitleCase(in.charAt(1)) || Character.isUpperCase(in.charAt(1)));
			return (useUpperCase ? Character.toUpperCase(first) : Character.toTitleCase(first)) + in.substring(1);
		}
	},
	BEANSPEC {
		@Override public String capitalize(String in) {
			if (in.length() == 0) return in;
			char first = in.charAt(0);
			if (!Character.isLowerCase(first) || (in.length() > 1 && Character.isUpperCase(in.charAt(1)))) return in;
			boolean useUpperCase = in.length() > 2 && Character.isTitleCase(in.charAt(1));
			return (useUpperCase ? Character.toUpperCase(first) : Character.toTitleCase(first)) + in.substring(1);
		}
	},
	;
	
	public static CapitalizationStrategy defaultValue() {
		return BASIC;
	}
	
	public abstract String capitalize(String in);
}
