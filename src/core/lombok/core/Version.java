/*
 * Copyright (C) 2009-2017 The Project Lombok Authors.
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

import java.io.InputStream;

/**
 * This class just holds lombok's current version.
 */
public class Version {
	// ** CAREFUL ** - this class must always compile with 0 dependencies (it must not refer to any other sources or libraries).
	// Note: In 'X.Y.Z', if Z is odd, its a snapshot build built from the repository, so many different 0.10.3 versions can exist, for example.
	// Official builds always end in an even number. (Since 0.10.2).
	private static final String VERSION = "1.16.19";
	private static final String RELEASE_NAME = "Edgy Guinea Pig";
//	private static final String RELEASE_NAME = "Dancing Elephant";
	
	// Named version history:
	//   Angry Butterfly
	//   Branching Cobra
	//   Candid Duck
	//   Dancing Elephant
	
	private Version() {
		//Prevent instantiation
	}
	
	/**
	 * Prints the version followed by a newline, and exits.
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.printf("%s\n", getFullVersion());
		} else {
			System.out.println(VERSION);
		}
	}
	
	/**
	 * Get the current Lombok version.
	 */
	public static String getVersion() {
		return VERSION;
	}
	
	/**
	 * Get the current release name.
	 * 
	 * The release name is a string (not numbers). Every time a new release has a significantly improved feature set, a new release name is given.
	 * Thus, many versions can carry the same release name. Version bumps and release names are not related; if a new version of lombok is entirely
	 * backwards compatible with a previous one, but also adds many new features, it will get only a minor version bump, but also a new release name.
	 */
	public static String getReleaseName() {
		return RELEASE_NAME;
	}
	
	public static String getFullVersion() {
		String version = String.format("v%s \"%s\"", VERSION, RELEASE_NAME);
		if (!isEdgeRelease()) return version;
		
		InputStream in = Version.class.getResourceAsStream("/release-timestamp.txt");
		if (in == null) return version;
		try {
			byte[] data = new byte[65536];
			int p = 0;
			while (p < data.length) {
				int r = in.read(data, p, data.length - p);
				if (r == -1) break;
				p += r;
			}
			
			String timestamp = new String(data, "UTF-8").trim();
			return version + " - " + timestamp;
		} catch (Exception e) {
			try {
				in.close();
			} catch (Exception ignore) {}
		}
		
		return version;
	}
	
	public static boolean isEdgeRelease() {
		int lastIdx = VERSION.lastIndexOf('.');
		if (lastIdx == -1) return false;
		try {
			return Integer.parseInt(VERSION.substring(lastIdx + 1)) % 2 == 1;
		} catch (Exception e) {
			return false;
		}
	}
}
