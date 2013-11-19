/*
 * Copyright (C) 2012-2013 The Project Lombok Authors.
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
package lombok;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.javac.CapturingDiagnosticListener.CompilerMessage;

public class CompilerMessageMatcher {
	/** Line Number (starting at 1) */
	private final long line;
	
	private final Collection<String> messageParts;
	
	public CompilerMessageMatcher(long line, String message) {
		this.line = line;
		this.messageParts = Arrays.asList(message.split("\\s+"));
	}
	
	public static CompilerMessageMatcher asCompilerMessageMatcher(CompilerMessage message) {
		return new CompilerMessageMatcher(message.getLine(), message.getMessage());
	}
	
	@Override public String toString() {
		StringBuilder parts = new StringBuilder();
		for (String part : messageParts) parts.append(part).append(" ");
		if (parts.length() > 0) parts.setLength(parts.length() - 1);
		return String.format("%d %s", line, parts);
	}
	
	public boolean matches(CompilerMessage message) {
		if (message.getLine() != this.line) return false;
		for (String token : messageParts) {
			if (!message.getMessage().contains(token)) return false;
		}
		return true;
	}
	
	public static List<CompilerMessageMatcher> readAll(InputStream rawIn) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(rawIn, "UTF-8"));
		List<CompilerMessageMatcher> out = new ArrayList<CompilerMessageMatcher>();
		for (String line = in.readLine(); line != null; line = in.readLine()) {
			CompilerMessageMatcher cmm = read(line);
			if (cmm != null) out.add(cmm);
		}
		return out;
	}
	
	private static final Pattern PATTERN = Pattern.compile("^(\\d+) (.*)$");
	private static CompilerMessageMatcher read(String line) {
		line = line.trim();
		if (line.isEmpty()) return null;
		Matcher m = PATTERN.matcher(line);
		if (!m.matches()) throw new IllegalArgumentException("Typo in test file: " + line);
		return new CompilerMessageMatcher(Integer.parseInt(m.group(1)), m.group(2));
	}
}
