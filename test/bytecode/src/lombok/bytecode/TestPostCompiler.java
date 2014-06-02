/*
 * Copyright (C) 2014 The Project Lombok Authors.
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

import java.io.File;
import java.io.IOException;

import lombok.core.DiagnosticsReceiver;
import lombok.core.PostCompiler;

import org.junit.Test;

public class TestPostCompiler {
	@Test
	public void testPostCompiler() throws IOException {
		byte[] compiled = TestClassFileMetaData.compile(new File("test/bytecode/resource/PostCompileSneaky.java"));
		DiagnosticsReceiver receiver = new DiagnosticsReceiver() {
			@Override public void addWarning(String message) {
				fail("Warning during post compilation processing of a sneakyThrow call: " + message);
			}
			
			@Override public void addError(String message) {
				fail("Error during post compilation processing of a sneakyThrow call: " + message);
			}
		};
		assertTrue("Before post compilation, expected lombok.Lombok.sneakyThrow() call in compiled code, but it's not there",
				new ClassFileMetaData(compiled).usesMethod("lombok/Lombok", "sneakyThrow"));
		byte[] transformed = PostCompiler.applyTransformations(compiled, "PostCompileSneaky.java", receiver);
		
		assertNotSame("Post-compiler did not do anything; we expected it to remove a Lombok.sneakyThrow() call.", compiled, transformed);
		assertTrue("After removing a sneakyThrow the classfile got... bigger (or stayed equal in size). Huh?", transformed.length < compiled.length);
	}
}
