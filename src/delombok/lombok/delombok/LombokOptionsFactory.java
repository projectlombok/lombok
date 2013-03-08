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
package lombok.delombok;

import lombok.javac.Javac6BasedLombokOptions;
import lombok.javac.Javac8BasedLombokOptions;
import lombok.javac.LombokOptions;

import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;

public class LombokOptionsFactory {
	
	enum LombokOptionCompilerVersion {
		JDK6_7 {
			@Override LombokOptions createAndRegisterOptions(Context context) {
				return Javac6BasedLombokOptions.replaceWithDelombokOptions(context);
			}
		},
		
		JDK8 {
			@Override LombokOptions createAndRegisterOptions(Context context) {
				return Javac8BasedLombokOptions.replaceWithDelombokOptions(context);
			}
		};
		
		abstract LombokOptions createAndRegisterOptions(Context context); 
	}
	
	
	public static LombokOptions getDelombokOptions(Context context) {
		LombokOptions options;
		if (JavaCompiler.version().startsWith("1.6") || JavaCompiler.version().startsWith("1.7")) {
			options = LombokOptionCompilerVersion.JDK6_7.createAndRegisterOptions(context);
		} else if (JavaCompiler.version().startsWith("1.8")) {
			options = LombokOptionCompilerVersion.JDK8.createAndRegisterOptions(context);
		} else {
			throw new IllegalStateException("No support for compiler version " + JavaCompiler.version() + " for delombok");
		}
		return options;
		
	}
}
