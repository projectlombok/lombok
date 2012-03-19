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

import java.io.InputStream;
import java.util.Arrays;

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
		} catch (Exception e) {
			return "java/lang/Object";
		} catch (ClassFormatError e) {
			ClassLoader cl = this.getClass().getClassLoader();
			if (cl == null) cl = ClassLoader.getSystemClassLoader();
			String message = debugCheckClassFormatErrorIssue(cl, type1) +
					debugCheckClassFormatErrorIssue(cl, type2);
			throw new ClassFormatError(message);
		}
	}
	
	
	// This is debug-aiding code in an attempt to find the cause of issue:
	// http://code.google.com/p/projectlombok/issues/detail?id=339
	private static String debugCheckClassFormatErrorIssue(ClassLoader cl, String type) {
		try {
			Class.forName(type.replace('/', '.'), false, cl);
			return String.format("Class.forName debug on %s: no issues\n", type);
		} catch (ClassFormatError e) {
			// expected
		} catch (Throwable e) {
			return String.format("Class.forName debug on %s: Exception: %s\n", type, e);
		}
		
		try {
			InputStream in = cl.getResourceAsStream(type + ".class");
			if (in == null) return String.format("Class.forName debug on %s: Can't find resource %s\n", type, type + ".class");
			try {
				int[] firstBytes = new int[4];
				for (int i = 0; i < 4; i++) firstBytes[0] = in.read();
				if (firstBytes[0] == -1) return String.format("Class.forName debug on %s: file size is 0\n", type);
				if (firstBytes[3] == -1) return String.format("Class.forName debug on %s: Less than 4 bytes in class file\n", type);
				if (!Arrays.equals(new int[] {0xCA, 0xFE, 0xBA, 0xBE}, firstBytes)) return String.format("Class.forName debug on %s: no CAFEBABE: %s\n", type, Arrays.toString(firstBytes));
				return String.format("Class.forName debug on %s: No immediately obvious reason for failure found\n", type);
			} finally {
				in.close();
			}
		} catch (Throwable e) {
			return String.format("Class.forName debug on %s: Can't read as stream: %s\n", type, e);
		}
	}
}