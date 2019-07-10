/*
 * Copyright (C) 2015 The Project Lombok Authors.
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
package lombok.eclipse.agent;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import lombok.core.DiagnosticsReceiver;
import lombok.core.PostCompiler;
import lombok.core.Version;

public class PatchFixesShadowLoaded {
	public static String addLombokNotesToEclipseAboutDialog(String origReturnValue, String key) {
		if ("aboutText".equals(key)) {
			if (origReturnValue.contains(" is installed. https://projectlombok.org")) return origReturnValue;
			return origReturnValue + "\n\nLombok " + Version.getFullVersion() + " is installed. https://projectlombok.org/";
		}
		return origReturnValue;
	}
	
	public static byte[] runPostCompiler(byte[] bytes, String fileName) {
		byte[] transformed = PostCompiler.applyTransformations(bytes, fileName, DiagnosticsReceiver.CONSOLE);
		return transformed == null ? bytes : transformed;
	}
	
	public static OutputStream runPostCompiler(OutputStream out) throws IOException {
		return PostCompiler.wrapOutputStream(out, "TEST", DiagnosticsReceiver.CONSOLE);
	}
	
	public static BufferedOutputStream runPostCompiler(BufferedOutputStream out, String path, String name) throws IOException {
		String fileName = path + "/" + name;
		return new BufferedOutputStream(PostCompiler.wrapOutputStream(out, fileName, DiagnosticsReceiver.CONSOLE));
	}
}
