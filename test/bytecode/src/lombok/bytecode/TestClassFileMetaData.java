/*
 * Copyright (C) 2010-2021 The Project Lombok Authors.
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import lombok.Lombok;

import org.junit.Test;

public class TestClassFileMetaData {
	
	private static ClassFileMetaData foo = create(new File("test/bytecode/resource/Foo.java"));
	private static ClassFileMetaData bar = create(new File("test/bytecode/resource/Bar.java"));
	private static ClassFileMetaData baz = create(new File("test/bytecode/resource/Baz.java"));
	private static ClassFileMetaData buux = create(new File("test/bytecode/resource/Buux.java"));
	
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
		assertTrue(foo.containsLong(0x1FFFFFFFFL));
		
		assertFalse(foo.containsLong(1));
		assertFalse(buux.containsLong(1));
	}
	
	private static ClassFileMetaData create(File file) {
		return new ClassFileMetaData(compile(file));
	}
	
	static byte[] compile(File file) {
		try {
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			if (compiler == null) {
				// The 'auto-find my compiler' code in java6 works because it hard-codes `c.s.t.j.a.JavacTool`, which we put on the classpath.
				// On java8, it fails, because it looks for tools.jar, which won't be there.
				// on J11+ place it succeeds again, as we run those on the real JDKs.
				
				// Thus, let's try this, in cae we're on java 8:
				
				try {
					compiler = (JavaCompiler) Class.forName("com.sun.tools.javac.api.JavacTool").getConstructor().newInstance();
				} catch (Exception e) {
					compiler = null;
				}
			}
			
			if (compiler == null) throw new RuntimeException("No javac tool is available in this distribution. Using an old JRE perhaps?");
			
			File tempDir = getTempDir();
			tempDir.mkdirs();
			List<String> options = Arrays.asList("-proc:none", "-d", tempDir.getAbsolutePath());
			
			StringWriter captureWarnings = new StringWriter();
			final StringBuilder compilerErrors = new StringBuilder();
			DiagnosticListener<JavaFileObject> diagnostics = new DiagnosticListener<JavaFileObject>() {
				@Override public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
					compilerErrors.append(diagnostic.toString()).append("\n");
				}
			};
			
			CompilationTask task = compiler.getTask(captureWarnings, null, diagnostics, options, null, Collections.singleton(new ContentBasedJavaFileObject(file.getPath(), readFileAsString(file))));
			Boolean taskResult = task.call();
			assertTrue("Compilation task didn't succeed: \n<Warnings and Errors>\n" + compilerErrors.toString() + "\n" + captureWarnings.toString() + "\n</Warnings and Errors>", taskResult);
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
			FileInputStream in = new FileInputStream(file);
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				StringWriter writer = new StringWriter();
				String line = reader.readLine();
				while(line != null) {
					writer.append(line).append("\n");
					line = reader.readLine();
				}
				reader.close();
				writer.close();
				return writer.toString();
			} finally {
				in.close();
			}
		} catch (Exception e) {
			throw Lombok.sneakyThrow(e);
		}
	}
}
