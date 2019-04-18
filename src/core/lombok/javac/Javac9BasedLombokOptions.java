/*
 * Copyright (C) 2017 The Project Lombok Authors.
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

import com.sun.tools.javac.main.Option;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Options;

public class Javac9BasedLombokOptions extends LombokOptions {
	public static Javac9BasedLombokOptions replaceWithDelombokOptions(Context context) {
		Options options = Options.instance(context);
		context.put(optionsKey, (Options) null);
		Javac9BasedLombokOptions result = new Javac9BasedLombokOptions(context);
		result.putAll(options);
		return result;
	}
	
	private Javac9BasedLombokOptions(Context context) {
		super(context);
	}
	
	@Override public void putJavacOption(String optionName, String value) {
		if (optionName.equals("CLASSPATH")) optionName = "CLASS_PATH";
		if (optionName.equals("SOURCEPATH")) optionName = "SOURCE_PATH";
		if (optionName.equals("BOOTCLASSPATH")) optionName = "BOOT_CLASS_PATH";
		String optionText = Option.valueOf(optionName).primaryName;
		put(optionText, value);
	}
}
