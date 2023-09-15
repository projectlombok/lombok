/*
 * Copyright (C) 2023 The Project Lombok Authors.
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
package lombok.launch;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Low-level shader tool, that inline 'rewrites' bytecode so that a given package name acts like it has a different name.
 * 
 * This is useful particularly in light of the java module system which disallows 'split packages' - different loader systems / class file sources
 * that both have the same package. Generally you'd take one of your dependency libraries (such as lombok's dependency on ObjectWeb's ASM bytecode parser) and
 * rename that dependency to avoid conflicts with other code that also wants to use ASM.
 * 
 * This shader doesn't use ASM or any other bytecode manip library; it just scans constant pools.
 * 
 * It's limited in its capabilities: A shading op (rename 'this' package to 'that') requires same-length strings so that the replacement can be done without moving things around.
 */
class PackageShader {
	private final byte[][] froms; // contains package prefixes that need shading.
	private final byte[][] tos;   // contains the post-shading package prefixes. froms.length == tos.length.
	
	/**
	 * @param shadeOps provide pairs of strings; each pair is a from,to tuple listing binary package prefixes, e.g. "Lorg/objectweb/asm", "Lorg/lombokweb/asm".
	 */
	public PackageShader(String... shadeOps) {
		if (shadeOps.length % 2 != 0) throw new IllegalArgumentException("Provide pairs: real package name to shaded package name (you provided an odd number of strings; even number required)");
		Charset ascii = Charset.forName("US-ASCII");
		int len = shadeOps.length / 2;
		froms = new byte[len][];
		tos = new byte[len][];
		for (int i = 0; i < len; i ++) {
			String in = shadeOps[i << 1];
			String out = shadeOps[(i << 1) | 1];
			if (in.contains(".") || out.contains(".")) throw new IllegalArgumentException("Binary name prefixes are required (use slashes and dollars instead of dots to separate type name elements); they look like e.g. 'java/util/'. Violating entry: " + in + " -> " + out);
			froms[i] = in.getBytes(ascii);
			tos[i] = out.getBytes(ascii);
			if (froms[i].length != tos[i].length) throw new IllegalArgumentException("Pair [" + in + " -> " + out + "] is invalid: Both strings must be the same length");
		}
	}
	
	private static final byte CONSTANTPOOLTYPE_UTF8 = 1;
	/**
	 * Fuxxes the constant pool of the provided bytecode, rewriting any constant pool entries that represent types that start with any of the registered package shade requests to the shaded variant.
	 * 
	 * The byte array is modified in-line.
	 * 
	 * @param b Bytecode
	 * @return {@code true} if at least one shading modification has been applied.
	 */
	public boolean apply(byte[] b) {
		ClassFileMetaData md = new ClassFileMetaData(b);
		boolean changes = false;
		
		 // We want to replace the package name but it can occur in the middle of a 'signature' string, e.g. ([I[Ljava/util/List;)Ljava/util/List; - so replace from start and _after_ every capital-L.
		// 'startPoints' tracks every location inside the constant pool UTF8 value that _could_ contain a fully qualified type name: Position 0 plus after _every_ capital-L in the UTF-8.
		int[] startPoints = new int[260];
		int maxStartPoints;
		
		for (int offset : md.getOffsets(CONSTANTPOOLTYPE_UTF8)) {
			int len = readValue(b, offset);
			offset += 2;
			startPoints[0] = offset;
			maxStartPoints = 1;
			
			for (int i = offset, max = offset + len; i < max; i++) {
				if (b[i] == 'L') startPoints[maxStartPoints++] = i + 1;
			}
			
			for (int i = 0; i < froms.length; i++) {
				outer: for (int startPointIdx = 0; startPointIdx < maxStartPoints; startPointIdx++) {
					int indexIntoSignature = startPoints[startPointIdx];
					if (len - (indexIntoSignature - offset) < froms[i].length) continue;
					for (int p = 0; p < froms[i].length; p++) {
						if (b[indexIntoSignature + p] != froms[i][p]) continue outer;
					}
					System.arraycopy(tos[i], 0, b, indexIntoSignature, tos[i].length);
					changes = true;
				}
			}
		}
		
		return changes;
	}
	
	/**
	 * Reads a 16-bit value at {@code position}
	 */
	private static int readValue(byte[] b, int position) {
		return ((b[position] & 0xFF) << 8) | (b[position + 1] & 0xFF);
	}
	
	public String reverseResourceName(String name) {
		outer: for (int i = 0; i < tos.length; i++) {
			int len = tos[i].length;
			if (name.length() < len) continue;
			for (int p = 0; p < len; p++) {
				if (name.charAt(p) != tos[i][p]) continue outer;
			}
			try {
				String out = new String(froms[i], 0, froms[i].length, "US-ASCII") + name.substring(len);
				return out;
			} catch (UnsupportedEncodingException e) {
				// Can't happen - US-ASCII is guaranteed by the JVM spec
				return name;
			}
		}
		
		return name;
	}
}
