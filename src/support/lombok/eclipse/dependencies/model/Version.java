/*
 * Copyright (C) 2024 The Project Lombok Authors.
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
package lombok.eclipse.dependencies.model;

public class Version {
	private int major;
	private int minor;
	private int micro;
	private String qualifier = "";
	
	public static Version parse(String v) {
		String[] parts = v.split("\\.", 4);
		
		Version version = new Version();
		version.major = Integer.parseInt(parts[0]);
		if (parts.length > 1) {
			version.minor = Integer.parseInt(parts[1]);
		}
		if (parts.length > 2) {
			version.micro = Integer.parseInt(parts[2]);
		}
		if (parts.length > 3) {
			version.qualifier = parts[3];
		}
		return version;
	}
	
	public int compareTo(Version other) {
		int result = major - other.major;
		if (result != 0) return result;
		
		result = minor - other.minor;
		if (result != 0) return result;
		
		result = micro - other.micro;
		if (result != 0) return result;
		
		return qualifier.compareTo(other.qualifier);
	}
	
	@Override
	public String toString() {
		String result = major + "." + minor + "." + micro;
		if (!qualifier.isEmpty()) result += "." + qualifier;
		return result;
	}
}
