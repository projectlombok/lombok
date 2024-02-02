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

public class VersionRange {
	public static final VersionRange ALL = VersionRange.parse("0.0.0");
	
	private Version min;
	private Version max;
	private boolean leftInclusive;
	private boolean leftExclusive;
	private boolean rightInclusive;
	private boolean rightExclusive;
	
	public static VersionRange parse(String v) {
		VersionRange versionRange = new VersionRange();
		versionRange.leftInclusive = v.startsWith("[");
		versionRange.leftExclusive = v.startsWith("(");
		versionRange.rightInclusive = v.endsWith("]");
		versionRange.rightExclusive = v.endsWith(")");
		
		if (!versionRange.leftInclusive && !versionRange.leftExclusive) {
			Version version = Version.parse(v);
			versionRange.min = version;
			versionRange.leftInclusive = true;
			return versionRange;
		}
		
		String[] parts = v.replaceAll("[\\[\\(\\)\\]]", "").split(",");
		versionRange.min = Version.parse(parts[0]);
		versionRange.max = Version.parse(parts[1]);
		return versionRange;
	}
	
	public boolean contains(Version version) {
		if (min != null) {
			int result = min.compareTo(version);
			if (result > 0) return false;
			if (result == 0 && !leftInclusive) return false;
		}
		if (max != null) {
			int result = max.compareTo(version);
			if (result < 0) return false;
			if (result == 0 && !rightInclusive) return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (leftInclusive) sb.append("[");
		if (leftExclusive) sb.append("(");
		if (min != null) sb.append(min);
		sb.append(",");
		if (max != null) sb.append(max);
		if (rightInclusive) sb.append("]");
		if (rightExclusive) sb.append(")");
		return sb.toString();
	}
}
