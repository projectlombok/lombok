/*
 * Copyright (C) 2012 The Project Lombok Authors.
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
package lombok.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import lombok.Lombok;
import lombok.patcher.ClassRootFinder;

import org.mangosdk.spi.ProviderFor;

@ProviderFor(LombokApp.class)
public class PublicApiCreatorApp extends LombokApp {
	@Override public String getAppName() {
		return "publicApi";
	}
	
	@Override public String getAppDescription() {
		return "Creates a small lombok-api.jar with the annotations and other public API\n" +
				"classes of all lombok features. This is primarily useful to include in your\n" +
				"android projects.";
	}
	
	@Override public int runApp(List<String> rawArgs) throws Exception {
		String loc = ".";
		switch (rawArgs.size()) {
		case 0: break;
		case 1: loc = rawArgs.get(0); break;
		default:
			System.err.println("Supply 1 path to specify the directory where lombok-api.jar will be created. No path means the current directory is used.");
			return 1;
		}
		
		File out = new File(loc, "lombok-api.jar");
		int errCode = 0;
		try {
			errCode = writeApiJar(out);
		} catch (Exception e) {
			System.err.println("ERROR: Creating " + canonical(out) + " failed: ");
			e.printStackTrace();
			return 1;
		}
		
		return errCode;
	}
	
	/**
	 * Returns a File object pointing to our own jar file. Will obviously fail if the installer was started via
	 * a jar that wasn't accessed via the file-system, or if its started via e.g. unpacking the jar.
	 */
	private static File findOurJar() {
		return new File(ClassRootFinder.findClassRootOfClass(PublicApiCreatorApp.class));
	}
	
	private int writeApiJar(File outFile) throws Exception {
		File selfRaw = findOurJar();
		if (selfRaw == null) {
			System.err.println("The publicApi option only works if lombok is a jar.");
			return 2;
		}
		
		List<String> toCopy = new ArrayList<String>();
		JarFile self = new JarFile(selfRaw);
		try {
			Enumeration<JarEntry> entries = self.entries();
			
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (!name.startsWith("lombok/")) continue;
				if (name.endsWith("/package-info.class")) continue;
				if (!name.endsWith(".class")) continue;
				
				String subName = name.substring(7, name.length() - 6);
				int firstSlash = subName.indexOf('/');
				if (firstSlash == -1) {
					// direct member of the lombok package.
					if (!subName.startsWith("ConfigurationKeys")) toCopy.add(name);
					continue;
				}
				String topPkg = subName.substring(0, firstSlash);
				if ("extern".equals(topPkg) || "experimental".equals(topPkg)) {
					toCopy.add(name);
				}
			}
		} finally {
			self.close();
		}
		
		if (toCopy.isEmpty()) {
			System.out.println("Not generating lombok-api.jar: No lombok api classes required!");
			return 1;
		}
		
		OutputStream out = new FileOutputStream(outFile);
		boolean success = false;
		try {
			JarOutputStream jar = new JarOutputStream(out);
			for (String resourceName : toCopy) {
				InputStream in = Lombok.class.getResourceAsStream("/" + resourceName);
				try {
					if (in == null) {
						throw new Fail(String.format("api class %s cannot be found", resourceName));
					}
					writeIntoJar(jar, resourceName, in);
				} finally {
					if (in != null) in.close();
				}
			}
			jar.close();
			out.close();
			
			System.out.println("Successfully created: " + canonical(outFile));
			
			return 0;
		} catch (Throwable t) {
			try { out.close();} catch (Throwable ignore) {}
			if (!success) outFile.delete();
			if (t instanceof Fail) {
				System.err.println(t.getMessage());
				return 1;
			} else if (t instanceof Exception) {
				throw (Exception)t;
			} else if (t instanceof Error) {
				throw (Error)t;
			} else {
				throw new Exception(t);
			}
		}
	}
	
	private void writeIntoJar(JarOutputStream jar, String resourceName, InputStream in) throws IOException {
		jar.putNextEntry(new ZipEntry(resourceName));
		byte[] b = new byte[65536];
		while (true) {
			int r = in.read(b);
			if (r == -1) break;
			jar.write(b, 0, r);
		}
		jar.closeEntry();
		in.close();
	}
	
	private static class Fail extends Exception {
		Fail(String message) {
			super(message);
		}
	}
	
	private static String canonical(File out) {
		try {
			return out.getCanonicalPath();
		} catch (Exception e) {
			return out.getAbsolutePath();
		}
	}
}
