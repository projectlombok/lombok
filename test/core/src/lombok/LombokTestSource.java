/*
 * Copyright (C) 2014 The Project Lombok Authors.
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;

import lombok.core.LombokImmutableList;
import lombok.core.configuration.BubblingConfigurationResolver;
import lombok.core.configuration.ConfigurationProblemReporter;
import lombok.core.configuration.ConfigurationResolver;
import lombok.core.configuration.StringConfigurationSource;

public class LombokTestSource {
	private final File file;
	private final String content;
	private final LombokImmutableList<CompilerMessageMatcher> messages;
	private final boolean ignore;
	private final int versionLowerLimit, versionUpperLimit;
	private final ConfigurationResolver configuration;
	
	public boolean versionWithinLimit(int version) {
		return version >= versionLowerLimit && version <= versionUpperLimit;
	}
	
	public File getFile() {
		return file;
	}
	
	public String getContent() {
		return content;
	}
	
	public LombokImmutableList<CompilerMessageMatcher> getMessages() {
		return messages;
	}
	
	public boolean isIgnore() {
		return ignore;
	}
	
	public ConfigurationResolver getConfiguration() {
		return configuration;
	}
	
	private static final Pattern VERSION_STYLE_1 = Pattern.compile("^(\\d+)$");
	private static final Pattern VERSION_STYLE_2 = Pattern.compile("^\\:(\\d+)$");
	private static final Pattern VERSION_STYLE_3 = Pattern.compile("^(\\d+):$");
	private static final Pattern VERSION_STYLE_4 = Pattern.compile("^(\\d+):(\\d+)$");
	
	private int[] parseVersionLimit(String spec) {
		/* Single version: '5' */ {
			Matcher m = VERSION_STYLE_1.matcher(spec);
			if (m.matches()) {
				int v = Integer.parseInt(m.group(1));
				return new int[] {v, v};
			}
		}
		
		/* Upper bound: ':5' (inclusive) */ {
			Matcher m = VERSION_STYLE_2.matcher(spec);
			if (m.matches()) return new int[] {0, Integer.parseInt(m.group(1))};
		}
		
		/* Lower bound '5:' (inclusive) */ {
			Matcher m = VERSION_STYLE_3.matcher(spec);
			if (m.matches()) return new int[] {Integer.parseInt(m.group(1)), 0};
		}
		
		/* Range '7:8' (inclusive) */ {
			Matcher m = VERSION_STYLE_4.matcher(spec);
			if (m.matches()) return new int[] {Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))};
		}
		
		return null;
	}
	
	private static final Pattern IGNORE_PATTERN = Pattern.compile("^\\s*ignore\\s*(?:[:-].*)?$", Pattern.CASE_INSENSITIVE);
	private LombokTestSource(File file, String content, List<CompilerMessageMatcher> messages, List<String> directives) {
		this.file = file;
		this.content = content;
		this.messages = messages == null ? LombokImmutableList.<CompilerMessageMatcher>of() : LombokImmutableList.copyOf(messages);
		
		StringBuilder conf = new StringBuilder();
		int versionLower = 0;
		int versionUpper = Integer.MAX_VALUE;
		boolean ignore = false;
		
		for (String directive : directives) {
			directive = directive.trim();
			String lc = directive.toLowerCase();
			if (IGNORE_PATTERN.matcher(directive).matches()) {
				ignore = true;
				continue;
			}
			
			if (lc.startsWith("version ")) {
				int[] limits = parseVersionLimit(lc.substring(7).trim());
				if (limits == null) {
					Assert.fail("Directive line \"" + directive + "\" in '" + file.getAbsolutePath() + "' invalid: version must be followed by a single integer.");
					throw new RuntimeException();
				}
				versionLower = limits[0];
				versionUpper = limits[1];
				continue;
			}
			
			if (lc.startsWith("conf:")) {
				String confLine = directive.substring(5).trim();
				conf.append(confLine).append("\n");
				continue;
			}
			
			Assert.fail("Directive line \"" + directive + "\" in '" + file.getAbsolutePath() + "' invalid: unrecognized directive.");
			throw new RuntimeException();
		}
		
		this.versionLowerLimit = versionLower;
		this.versionUpperLimit = versionUpper;
		this.ignore = ignore;
		ConfigurationProblemReporter reporter = new ConfigurationProblemReporter() {
			@Override public void report(String sourceDescription, String problem, int lineNumber, CharSequence line) {
				Assert.fail("Problem on directive line: " + problem + " at conf line #" + lineNumber + " (" + line + ")");
			}
		};
		
		this.configuration = new BubblingConfigurationResolver(Collections.singleton(StringConfigurationSource.forString(conf, reporter, file.getAbsolutePath())));
	}
	
	public static LombokTestSource readDirectives(File file) throws IOException {
		List<String> directives = new ArrayList<String>();
		
		{
			@Cleanup val rawIn = new FileInputStream(file);
			BufferedReader in = new BufferedReader(new InputStreamReader(rawIn, "UTF-8"));
			for (String i = in.readLine(); i != null; i = in.readLine()) {
				if (i.isEmpty()) continue;
				
				if (i.startsWith("//")) {
					directives.add(i.substring(2));
				} else {
					break;
				}
			}
			in.close();
			rawIn.close();
		}
		
		return new LombokTestSource(file, "", null, directives);
	}
	
	public static LombokTestSource read(File sourceFolder, File messagesFolder, String fileName) throws IOException {
		StringBuilder content = null;
		List<String> directives = new ArrayList<String>();
		
		File sourceFile = new File(sourceFolder, fileName);
		if (sourceFile.exists()) {
			@Cleanup val rawIn = new FileInputStream(sourceFile);
			BufferedReader in = new BufferedReader(new InputStreamReader(rawIn, "UTF-8"));
			for (String i = in.readLine(); i != null; i = in.readLine()) {
				if (content != null) {
					content.append(i).append("\n");
					continue;
				}
				
				if (i.isEmpty()) continue;
				
				if (i.startsWith("//")) {
					directives.add(i.substring(2));
				} else {
					content = new StringBuilder();
					content.append(i).append("\n");
				}
			}
			in.close();
			rawIn.close();
		}
		
		if (content == null) content = new StringBuilder();
		
		List<CompilerMessageMatcher> messages = null;
		if (messagesFolder != null) {
			File messagesFile = new File(messagesFolder, fileName + ".messages");
			try {
				@Cleanup val rawIn = new FileInputStream(messagesFile);
				messages = CompilerMessageMatcher.readAll(rawIn);
				rawIn.close();
			} catch (FileNotFoundException e) {
				messages = null;
			}
		}
		
		return new LombokTestSource(sourceFile, content.toString(), messages, directives);
	}
}
