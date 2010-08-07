/*
 * Copyright © 2010 Reinier Zwitserloot and Roel Spilker.
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

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import lombok.Lombok;

import org.junit.Test;

public class ClassFileMetaDataTest {
	
	private static ClassFileMetaData foo = create(new File("test/bytecode/resource/Foo.java"));
	private static ClassFileMetaData bar = create(new File("test/bytecode/resource/Bar.java"));
	private static ClassFileMetaData baz = create(new File("test/bytecode/resource/Baz.java"));
	private static ClassFileMetaData buux = create(new File("test/bytecode/resource/Buux.java"));
	
//	@Test 
//	public void dump() {
//		byte[] bytes = compile(new File("test/bytecode/resource/Foo.java"));
//		int count = 0;
//		for (byte b : bytes) {
//			System.out.printf("%02x ", (b & 0xFF));
//			count++;
//			if (count % 20 == 0) System.out.println();
//		}
//		System.out.println();
//		System.out.println();
//		System.out.println(foo.poolContent());
//	}
	
	@Test
	public void testGetClassName() {
		assertTrue(foo.containsUtf8("Foo"));
		assertEquals("Foo", foo.getClassName());

		assertTrue(bar.containsUtf8("Bar"));
		assertEquals("Bar", bar.getClassName());
		
		assertTrue(baz.containsUtf8("Baz"));
		assertEquals("Baz", baz.getClassName());
	}
	
	@Test
	public void testGetSuperClassName() {
		assertTrue(foo.containsUtf8("java/lang/Object"));
		assertEquals("java/lang/Object", foo.getSuperClassName());

		assertEquals("java/lang/Object", bar.getSuperClassName());
		assertEquals("java/lang/Object", baz.getSuperClassName());
		
		assertEquals("java/util/ArrayList", buux.getSuperClassName());
	}
	
	
	
	@Test
	public void testUsesClass() {
		assertTrue(foo.usesClass("java/lang/System"));
//		assertTrue(foo.usesClass("java/lang/String"));
	}
	
	@Test
	public void testUsesField() {
		assertTrue(foo.usesField("java/lang/System", "out"));
	}
	
	@Test
	public void testUsesMethodWithName() {
		assertTrue(foo.usesMethod("java/io/PrintStream", "print"));
		
		assertTrue(buux.usesMethod("java/util/ArrayList", "<init>"));
		assertTrue(buux.usesMethod("java/util/ArrayList", "add"));
		assertTrue(buux.usesMethod("Buux", "addSomething"));
	}
	
	@Test
	public void testUsesMethodWithNameAndDescriptor() {
		assertTrue(foo.usesMethod("java/io/PrintStream", "print", "(Ljava/lang/String;)V"));
		
		assertTrue(buux.usesMethod("java/util/ArrayList", "<init>", "(I)V"));
		assertTrue(buux.usesMethod("java/util/ArrayList", "add", "(Ljava/lang/Object;)Z"));
		assertTrue(buux.usesMethod("Buux", "addSomething", "()V"));
	}
	
	@Test
	public void testGetInterfaces() {
		assertTrue(foo.containsUtf8("java/util/RandomAccess"));
		
		List<String> fooInterfaces = foo.getInterfaces();
		assertEquals(1, fooInterfaces.size());
		assertEquals("java/util/RandomAccess", fooInterfaces.get(0));
		
		assertTrue(bar.containsUtf8("java/util/RandomAccess"));
		assertTrue(bar.containsUtf8("java/util/Map"));
		
		List<String> barInterfaces = bar.getInterfaces();
		assertEquals(2, barInterfaces.size());
		assertEquals("java/util/RandomAccess", barInterfaces.get(0));
		assertEquals("java/util/Map", barInterfaces.get(1));
	}
	
	@Test
	public void testContainsStringConstant() {
		assertTrue(foo.containsStringConstant("Eén"));
		assertTrue(foo.containsStringConstant("TwoFour"));
		
		assertTrue(buux.containsStringConstant("H\u3404l\0"));
		
		assertFalse(foo.containsStringConstant("Seven"));
	}
	
	@Test
	public void testContainsDouble() {
		assertTrue(foo.containsDouble(1.23));
		assertTrue(foo.containsDouble(Double.NaN));
		assertTrue(foo.containsDouble(Double.POSITIVE_INFINITY));
		assertTrue(foo.containsDouble(Double.NEGATIVE_INFINITY));
		
		assertFalse(foo.containsDouble(1.0));
		assertFalse(buux.containsDouble(1.0));
		assertFalse(buux.containsDouble(Double.NaN));
		assertFalse(buux.containsDouble(Double.POSITIVE_INFINITY));
		assertFalse(buux.containsDouble(Double.NEGATIVE_INFINITY));
	}	
	
	@Test
	public void testContainsFloat() {
		assertTrue(foo.containsFloat(1.23F));
		assertTrue(foo.containsFloat(Float.NaN));
		assertTrue(foo.containsFloat(Float.POSITIVE_INFINITY));
		assertTrue(foo.containsFloat(Float.NEGATIVE_INFINITY));
		
		assertFalse(foo.containsFloat(1.0F));
		assertFalse(buux.containsFloat(1.0F));
		assertFalse(buux.containsFloat(Float.NaN));
		assertFalse(buux.containsFloat(Float.POSITIVE_INFINITY));
		assertFalse(buux.containsFloat(Float.NEGATIVE_INFINITY));
	}	
	
	@Test
	public void testContainsInteger() {
		assertTrue(foo.containsInteger(123));
		
		assertFalse(foo.containsInteger(1));
		assertFalse(buux.containsInteger(1));
	}
	
	@Test
	public void testContainsLong() {
		assertTrue(foo.containsLong(123));
		
		assertFalse(foo.containsLong(1));
		assertFalse(buux.containsLong(1));
	}
	
	private static ClassFileMetaData create(File file) {
		return new ClassFileMetaData(compile(file));
	}
	
	private static byte[] compile(File file) {
		try {
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			File tempDir = getTempDir();
			tempDir.mkdirs();
			List<String> options = Arrays.asList("-nowarn", "-proc:none", "-d", tempDir.getAbsolutePath());
			StringWriter captureWarnings = new StringWriter();
			CompilationTask task = compiler.getTask(captureWarnings, null, null, options, null, Collections.singleton(new ContentBasedJavaFileObject(file.getPath(), readFileAsString(file))));
			assertTrue(task.call());
			return PostCompilerApp.readFile(new File(tempDir, file.getName().replaceAll("\\.java$", ".class")));
		} catch (Exception e) {
			throw Lombok.sneakyThrow(e);
		}
	}
	
	private static File getTempDir() {
		String[] rawDirs = {
				System.getProperty("java.io.tmpdir"),
				"/tmp",
				"C:\\Windows\\Temp"
		};
		
		for (String dir : rawDirs) {
			if (dir == null) continue;
			File f = new File(dir);
			if (!f.isDirectory()) continue;
			return new File(f, "lombok.bytecode-test");
		}
		
		return new File("./build/tmp");
	}
	
	static class ContentBasedJavaFileObject extends SimpleJavaFileObject {
		private final String content;
		
		protected ContentBasedJavaFileObject(String name, String content) {
			super(new File(name).toURI(), Kind.SOURCE);
			this.content = content;
		}
		
		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return content;
		}
	}
	
	private static String readFileAsString(File file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			StringWriter writer = new StringWriter();
			String line = reader.readLine();
			while(line != null) {
				writer.append(line).append("\n");
				line = reader.readLine();
			}
			reader.close();
			writer.close();
			return writer.toString();
		} catch (Exception e) {
			throw Lombok.sneakyThrow(e);
		}
	}
}
