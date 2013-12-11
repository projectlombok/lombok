/*
 * Copyright (C) 2013 The Project Lombok Authors.
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
package lombok.delombok;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Scans a java source file to figure out certain format preferences.
 * Currently:<ul>
 * <li>Indent style (tab or 2-8 spaces are supported). Default: tab</li>
 * <li>Empty lines still carry the appropriate indent, vs. empty lines are empty. Default: empty</li>
 */
public class FormatPreferenceScanner {
	/** Checks validity of preferences, and returns with a non-null value if ALL format keys are available, thus negating the need for a scan. */
	private FormatPreferences tryEasy(FormatPreferences preferences, boolean force) {
		int count = 0;
		for (Map.Entry<String, String> e : preferences.rawMap.entrySet()) {
			if (!"scan".equalsIgnoreCase(e.getValue())) count++;
		}
		if (force || count >= FormatPreferences.KEYS.size()) return preferences;
		return null;
	}
	
	public FormatPreferences scan(FormatPreferences preferences, final CharSequence source) {
		FormatPreferences fps = tryEasy(preferences, source == null);
		if (fps != null) return fps;
		
		try {
			return scan_(preferences, new Reader() {
				int pos = 0;
				int max = source.length();
				
				@Override public void close() throws IOException {
				}
				
				@Override public int read(char[] b, int p, int len) throws IOException {
					int read = 0;
					if (pos >= max) return -1;
					for (int i = p; i < p + len; i++) {
						b[i] = source.charAt(pos++);
						read++;
						if (pos == max) return read;
					}
					return len;
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e); //Can't happen; CAR doesn't throw these.
		}
	}
	
	public FormatPreferences scan(FormatPreferences preferences, char[] source) {
		FormatPreferences fps = tryEasy(preferences, source == null);
		if (fps != null) return fps;
		
		try {
			return scan_(preferences, new CharArrayReader(source));
		} catch (IOException e) {
			throw new RuntimeException(e); //Can't happen; CAR doesn't throw these.
		}
	}
	
	public FormatPreferences scan(FormatPreferences preferences, Reader in) throws IOException {
		FormatPreferences fps = tryEasy(preferences, in == null);
		if (fps != null) return fps;
		
		return scan_(preferences, in);
	}
	
	private static FormatPreferences scan_(FormatPreferences preferences, Reader in) throws IOException {
		int filledEmpties = 0;
		List<String> indents = new ArrayList<String>();
		
		char[] buffer = new char[32700];
		int pos = 1;
		int end = 0;
		
		StringBuilder indentSoFar = new StringBuilder();
		boolean inIndent = true;
		boolean inComment = false;
		char lastChar = ' ';
		
		while (true) {
			if (pos >= end) {
				int r = in.read(buffer);
				if (r == -1) break;
				pos = 0;
				end = r;
				continue;
			}
			
			char c = buffer[pos++];
			if (inComment) {
				if (lastChar == '*' && c == '/') inComment = false;
				lastChar = c;
				continue;
			}
			
			if (lastChar == '/' && c == '*') {
				inComment = true;
				lastChar = ' ';
				indentSoFar.setLength(0);
				inIndent = false;
				continue;
			}
			
			if (inIndent) {
				boolean w = Character.isWhitespace(c);
				if (c == '\n') {
					if (indentSoFar.length() > 0 && indentSoFar.charAt(indentSoFar.length() -1) == '\r') {
						indentSoFar.setLength(indentSoFar.length() - 1);
					}
					
					if (indentSoFar.length() > 0) {
						filledEmpties++;
					} else {
					}
					indents.add(indentSoFar.toString());
					indentSoFar.setLength(0);
					lastChar = c;
					continue;
				}
				
				if (w) {
					indentSoFar.append(c);
					lastChar = c;
					continue;
				}
				
				if (indentSoFar.length() > 0) {
					indents.add(indentSoFar.toString());
					indentSoFar.setLength(0);
				}
				lastChar = c;
				inIndent = false;
				continue;
			}
			
			lastChar = c;
			if (c == '\n') {
				inIndent = true;
				indentSoFar.setLength(0);
			}
		}
		
		String indent = null;
		int lowestSpaceCount = Integer.MAX_VALUE;
		for (String ind : indents) {
			if (ind.indexOf('\t') > -1) {
				indent = "\t";
				break;
			}
			if (ind.length() < 2 || ind.length() > 8) continue;
			if (ind.length() < lowestSpaceCount) lowestSpaceCount = ind.length();
		}
		
		if (lowestSpaceCount == Integer.MAX_VALUE) indent = "\t";
		
		if (indent == null) {
			char[] id = new char[lowestSpaceCount];
			Arrays.fill(id, ' ');
			indent = new String(id);
		}
		
		return new FormatPreferences(preferences.rawMap, indent, filledEmpties > 0);
	}
}
