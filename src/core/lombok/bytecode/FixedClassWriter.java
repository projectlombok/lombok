/*
 * Copyright (C) 2010-2012 The Project Lombok Authors.
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
package lombok.bytecode;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

class FixedClassWriter extends ClassWriter {
	FixedClassWriter(ClassReader classReader, int flags) {
		super(classReader, flags);
	}
	
	@Override protected String getCommonSuperClass(String type1, String type2) {
		//By default, ASM will attempt to live-load the class types, which will fail if meddling with classes in an
		//environment with custom classloaders, such as Equinox. It's just an optimization; returning Object is always legal.
		try {
			return super.getCommonSuperClass(type1, type2);
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (Throwable t) {
			return "java/lang/Object";
		}
	}
}