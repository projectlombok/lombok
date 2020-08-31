/*
 * Copyright (C) 2009-2016 The Project Lombok Authors.
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
package lombok.installer;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import lombok.patcher.ClassRootFinder;

/**
 * Represents a location that contains an IDE.
 */
public abstract class IdeLocation {
	/** Toggling the 'selected' checkbox in the GUI is tracked via this boolean */
	boolean selected = true;
	
	public abstract String install() throws InstallException;
	public abstract void uninstall() throws UninstallException;
	public abstract String getName();
	public abstract boolean hasLombok();
	public abstract URL getIdeIcon();
	
	/**
	 * Returns a File object pointing to our own jar file. Will obviously fail if the installer was started via
	 * a jar that wasn't accessed via the file-system, or if its started via e.g. unpacking the jar.
	 */
	public static File findOurJar() {
		return new File(ClassRootFinder.findClassRootOfClass(OsUtils.class));
	}
	
	@Override public String toString() {
		return getName();
	}
	
	/**
	 * Returns a full path to the provided file.
	 * Returns the canonical path, unless that is not available, in which case it returns the absolute path.
	 */
	public static String canonical(File p) {
		try {
			return p.getCanonicalPath();
		} catch (IOException e) {
			String x = p.getAbsolutePath();
			return x == null ? p.getPath() : x;
		}
	}
	
	private static final String LEGAL_PATH_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.-_/";
	private static final String LEGAL_PATH_CHARS_WINDOWS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,/;'[]{}!@#$^&()-_+= :\\";
	public static String escapePath(String path) {
		StringBuilder out = new StringBuilder();
		String legalChars = OsUtils.getOS() == OsUtils.OS.UNIX ? LEGAL_PATH_CHARS : LEGAL_PATH_CHARS_WINDOWS; 
		for (char c : path.toCharArray()) {
			if (legalChars.indexOf(c) == -1) out.append('\\');
			out.append(c);
		}
		return out.toString();
	}
}
