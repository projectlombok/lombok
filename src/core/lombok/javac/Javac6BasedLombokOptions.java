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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.Lombok;

import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Options;

public class Javac6BasedLombokOptions extends LombokOptions {
	private static final Method optionName_valueOf;
	private static final Method options_put;
	
	static {
		try {
			Class<?> optionNameClass = Class.forName("com.sun.tools.javac.main.OptionName");
			optionName_valueOf = optionNameClass.getMethod("valueOf", String.class);
			options_put = Class.forName("com.sun.tools.javac.util.Options").getMethod("put", optionNameClass, String.class);
		} catch (Exception e) {
			throw new IllegalArgumentException("Can't initialize Javac6-based lombok options due to reflection issue.", e);
		}
	}
	
	public static Javac6BasedLombokOptions replaceWithDelombokOptions(Context context) {
		Options options = Options.instance(context);
		context.put(optionsKey, (Options)null);
		Javac6BasedLombokOptions result = new Javac6BasedLombokOptions(context);
		result.putAll(options);
		return result;
	}
	
	private Javac6BasedLombokOptions(Context context) {
		super(context);
	}
	
	@Override public void putJavacOption(String optionName, String value) {
		try {
			options_put.invoke(this, optionName_valueOf.invoke(null, optionName), value);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Can't initialize Javac6-based lombok options due to reflection issue.", e);
		} catch (InvocationTargetException e) {
			throw Lombok.sneakyThrow(e.getCause());
		}
	}
}
