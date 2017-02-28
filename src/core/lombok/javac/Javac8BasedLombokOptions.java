/*
 * Copyright (C) 2013 The Project Lombok Authors.
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
package lombok.javac;

import java.lang.reflect.Field;

import com.sun.tools.javac.main.Option;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Options;

public class Javac8BasedLombokOptions extends LombokOptions {
	private static class Namer {
		static final Field nameField;
		
		static {
			Field f = null;
			try {
				// jdk9
				f = Option.class.getField("primaryName");
			}
			catch (NoSuchFieldException e) {
				try {
					f = Option.class.getField("text");
				}
				catch (NoSuchFieldException e2) {}
			}
			nameField = f;
		}
		
		static String name(Option option) {
			try {
				return (String)nameField.get(option);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static Javac8BasedLombokOptions replaceWithDelombokOptions(Context context) {
		Options options = Options.instance(context);
		context.put(optionsKey, (Options)null);
		Javac8BasedLombokOptions result = new Javac8BasedLombokOptions(context);
		result.putAll(options);
		return result;
	}
	
	private Javac8BasedLombokOptions(Context context) {
		super(context);
	}
	
	@Override public void putJavacOption(String optionName, String value) {
		String optionText = Namer.name(Option.valueOf(optionName));
		put(optionText, value);
	}
}
