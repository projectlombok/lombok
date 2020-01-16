/*
 * Copyright (C) 2014-2019 The Project Lombok Authors.
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;

import lombok.core.LombokImmutableList;
import lombok.core.configuration.BubblingConfigurationResolver;
import lombok.core.configuration.ConfigurationFile;
import lombok.core.configuration.ConfigurationFileToSource;
import lombok.core.configuration.ConfigurationParser;
import lombok.core.configuration.ConfigurationProblemReporter;
import lombok.core.configuration.ConfigurationResolver;
import lombok.core.configuration.ConfigurationSource;
import lombok.core.configuration.SingleConfigurationSource;

public class LombokTestSource {
	private final File file;
	private final String content;
	private final LombokImmutableList<CompilerMessageMatcher> messages;
	private final Map<String, String> formatPreferences;
	private final boolean ignore;
	private final boolean skipCompareContent;
	private final boolean skipIdempotent;
	private final boolean unchanged;
	private final int versionLowerLimit, versionUpperLimit;
	private final ConfigurationResolver configuration;
	private final String specifiedEncoding;
	private final List<String> platforms;

	public boolean runOnPlatform(String platform) {
		if (platforms == null || platforms.isEmpty()) return true;
		for (String pl : platforms) if (pl.equalsIgnoreCase(platform)) return true;
		return false;
	}
	
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
	
	public boolean forceUnchanged() {
		return unchanged;
	}
	
	public boolean isSkipCompareContent() {
		return skipCompareContent;
	}
	
	public boolean isSkipIdempotent() {
		return skipIdempotent;
	}
	
	public String getSpecifiedEncoding() {
		return specifiedEncoding;
	}
	
	public ConfigurationResolver getConfiguration() {
		return configuration;
	}
	
	public Map<String, String> getFormatPreferences() {
		return formatPreferences;
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
			if (m.matches()) return new int[] {Integer.parseInt(m.group(1)), Integer.MAX_VALUE};
		}
		
		/* Range '7:8' (inclusive) */ {
			Matcher m = VERSION_STYLE_4.matcher(spec);
			if (m.matches()) return new int[] {Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))};
		}
		
		return null;
	}
	
	private static final Pattern IGNORE_PATTERN = Pattern.compile("^\\s*ignore\\s*(?:[-:].*)?$", Pattern.CASE_INSENSITIVE);
	private static final Pattern UNCHANGED_PATTERN = Pattern.compile("^\\s*unchanged\\s*(?:[-:].*)?$", Pattern.CASE_INSENSITIVE);
	private static final Pattern SKIP_COMPARE_CONTENT_PATTERN = Pattern.compile("^\\s*skip[- ]?compare[- ]?contents?\\s*(?:[-:].*)?$", Pattern.CASE_INSENSITIVE);
	private static final Pattern SKIP_IDEMPOTENT_PATTERN = Pattern.compile("^\\s*skip[- ]?idempotent\\s*(?:[-:].*)?$", Pattern.CASE_INSENSITIVE);
	
	private LombokTestSource(File file, String content, List<CompilerMessageMatcher> messages, List<String> directives) {
		this.file = file;
		this.content = content;
		this.messages = messages == null ? LombokImmutableList.<CompilerMessageMatcher>of() : LombokImmutableList.copyOf(messages);
		
		StringBuilder conf = new StringBuilder();
		int versionLower = 0;
		int versionUpper = Integer.MAX_VALUE;
		boolean ignore = false;
		boolean skipCompareContent = false;
		boolean skipIdempotent = false;
		boolean unchanged = false;
		String encoding = null;
		Map<String, String> formats = new HashMap<String, String>();
		String[] platformLimit = null;
		
		for (String directive : directives) {
			directive = directive.trim();
			String lc = directive.toLowerCase();
			if (IGNORE_PATTERN.matcher(directive).matches()) {
				ignore = true;
				continue;
			}
			
			if (UNCHANGED_PATTERN.matcher(directive).matches()) {
				unchanged = true;
				continue;
			}
			
			if (SKIP_COMPARE_CONTENT_PATTERN.matcher(directive).matches()) {
				skipCompareContent = true;
				continue;
			}
			
			if (SKIP_IDEMPOTENT_PATTERN.matcher(directive).matches()) {
				skipIdempotent = true;
				continue;
			}
			
			if (lc.startsWith("platform ")) {
				String platformDesc = lc.substring("platform ".length());
				int idx = platformDesc.indexOf(':');
				if (idx != -1) platformDesc = platformDesc.substring(0, idx).trim();
				platformLimit = platformDesc.split("\\s*,\\s*");
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
			
			if (lc.startsWith("encoding:")) {
				encoding = directive.substring(9).trim();
				continue;
			}
			
			if (lc.startsWith("format:")) {
				String formatLine = directive.substring(7).trim();
				int idx = formatLine.indexOf('=');
				if (idx == -1) throw new IllegalArgumentException("To add a format directive, use: \"//FORMAT: javaLangAsFQN = skip\"");
				String key = formatLine.substring(0, idx).trim();
				String value = formatLine.substring(idx + 1).trim();
				formats.put(key.toLowerCase(), value);
				continue;
			}
			
			if (lc.startsWith("issue ")) continue;
			
			Assert.fail("Directive line \"" + directive + "\" in '" + file.getAbsolutePath() + "' invalid: unrecognized directive.");
			throw new RuntimeException();
		}
		this.specifiedEncoding = encoding;
		this.versionLowerLimit = versionLower;
		this.versionUpperLimit = versionUpper;
		this.ignore = ignore;
		this.skipCompareContent = skipCompareContent;
		this.skipIdempotent = skipIdempotent;
		this.unchanged = unchanged;
		this.platforms = platformLimit == null ? null : Arrays.asList(platformLimit);
		
		ConfigurationProblemReporter reporter = new ConfigurationProblemReporter() {
			@Override public void report(String sourceDescription, String problem, int lineNumber, CharSequence line) {
				Assert.fail("Problem on directive line: " + problem + " at conf line #" + lineNumber + " (" + line + ")");
			}
		};
		final ConfigurationFile configurationFile = ConfigurationFile.fromCharSequence(file.getAbsoluteFile().getPath(), conf, ConfigurationFile.getLastModifiedOrMissing(file));
		final ConfigurationSource source = SingleConfigurationSource.parse(configurationFile, new ConfigurationParser(reporter));
		ConfigurationFileToSource sourceFinder = new ConfigurationFileToSource() {
			@Override public ConfigurationSource parsed(ConfigurationFile fileLocation) {
				return fileLocation.equals(configurationFile) ? source : null;
			}
		};
		
		this.configuration = new BubblingConfigurationResolver(configurationFile, sourceFinder);
		this.formatPreferences = Collections.unmodifiableMap(formats);
	}
	
	public static LombokTestSource readDirectives(File file) throws IOException {
		List<String> directives = new ArrayList<String>();
		
		{
			InputStream rawIn = new FileInputStream(file);
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(rawIn, "UTF-8"));
				try {
					for (String i = in.readLine(); i != null; i = in.readLine()) {
						if (i.isEmpty()) continue;
						
						if (i.startsWith("//")) {
							directives.add(i.substring(2));
						} else {
							break;
						}
					}
				}
				finally {
					in.close();
				}
			}
			finally {
				rawIn.close();
			}
		}
		
		return new LombokTestSource(file, "", null, directives);
	}
	
	public static LombokTestSource read(File sourceFolder, File messagesFolder, String fileName) throws IOException {
		return read0(sourceFolder, messagesFolder, fileName, "UTF-8");
	}
	
	private static LombokTestSource read0(File sourceFolder, File messagesFolder, String fileName, String encoding) throws IOException {
		StringBuilder content = null;
		List<String> directives = new ArrayList<String>();
		
		File sourceFile = new File(sourceFolder, fileName);
		if (sourceFile.exists()) {
			InputStream rawIn = new FileInputStream(sourceFile);
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(rawIn, encoding));
				try {
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
				}
				finally {
					in.close();
				}
			}
			finally {
				rawIn.close();
			}
		}
		
		if (content == null) content = new StringBuilder();
		
		List<CompilerMessageMatcher> messages = null;
		if (messagesFolder != null) {
			File messagesFile = new File(messagesFolder, fileName + ".messages");
			try {
				InputStream rawIn = new FileInputStream(messagesFile);
				try {
					messages = CompilerMessageMatcher.readAll(rawIn);
				}
				finally {
					rawIn.close();
				}
			} catch (FileNotFoundException e) {
				messages = null;
			}
		}
		
		LombokTestSource source = new LombokTestSource(sourceFile, content.toString(), messages, directives);
		String specifiedEncoding = source.getSpecifiedEncoding();
		
		// The source file has an 'encoding' header to test encoding issues. Of course, reading the encoding header
		// requires knowing the encoding of the file first. In practice we get away with it, because UTF-8 and US-ASCII are compatible enough.
		// The fix is therefore to read in as UTF-8 initially, and if the file requests that it should be read as another encoding, toss it all
		// and reread that way.
		
		if (specifiedEncoding == null || specifiedEncoding.equalsIgnoreCase(encoding)) return source;
		return read0(sourceFolder, messagesFolder, fileName, specifiedEncoding);
	}
}
