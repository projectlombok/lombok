/*
 * Copyright (C) 2012-2024 The Project Lombok Authors.
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.javac.CapturingDiagnosticListener.CompilerMessage;

public class CompilerMessageMatcher {
	/** Line Number (starting at 1) */
	private final List<Integer> lineNumbers = new ArrayList<Integer>();
	private final List<Long> positions = new ArrayList<Long>();
	private final List<List<String>> messages = new ArrayList<List<String>>();
	private boolean optional;
	
	private CompilerMessageMatcher() {}
	
	public boolean isOptional() {
		return optional;
	}
	
	public static CompilerMessageMatcher asCompilerMessageMatcher(CompilerMessage message) {
		CompilerMessageMatcher cmm = new CompilerMessageMatcher();
		cmm.lineNumbers.add((int) message.getLine());
		cmm.positions.add(message.getPosition());
		cmm.messages.add(Arrays.asList(message.getMessage().split("\\s+")));
		return cmm;
	}
	
	@Override public String toString() {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < lineNumbers.size(); i++) {
			out.append(lineNumbers.get(i));
			if (positions.get(i) != null) out.append(":").append(positions.get(i));
			out.append(" ");
			for (String part : messages.get(i)) out.append(part).append(" ");
			if (out.length() > 0) out.setLength(out.length() - 1);
			out.append(" |||| ");
		}
		if (out.length() > 0) out.setLength(out.length() - 6);
		return out.toString();
	}
	
	public boolean matches(CompilerMessage message) {
		outer:
		for (int i = 0; i < lineNumbers.size(); i++) {
			if (message.getLine() != lineNumbers.get(i)) continue;
			if (positions.get(i) != null && !positions.get(i).equals(message.getPosition())) continue;
			for (String token : messages.get(i)) {
				if (!message.getMessage().contains(token)) continue outer;
			}
			return true;
		}
		
		return false;
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
	
	private static final Pattern PATTERN = Pattern.compile("^(-?\\d+)(?::(\\d+))? (.*)$");
	
	private static CompilerMessageMatcher read(String line) {
		line = line.trim();
		if (line.isEmpty()) return null;
		boolean optional = false;
		
		if (line.startsWith("OPTIONAL ")) {
			line = line.substring(9);
			optional = true;
		}
		
		String[] parts = line.split("\\s*\\|\\|\\|\\|\\s*");
		
		CompilerMessageMatcher cmm = new CompilerMessageMatcher();
		cmm.optional = optional;
		for (String part : parts) {
			Matcher m = PATTERN.matcher(part);
			if (!m.matches()) throw new IllegalArgumentException("Typo in test file: " + line);
			cmm.lineNumbers.add(Integer.parseInt(m.group(1)));
			cmm.positions.add(m.group(2) != null ? Long.parseLong(m.group(2)) : null);
			cmm.messages.add(Arrays.asList(m.group(3).split("\\s+")));
		}
		
		return cmm;
	}
}
