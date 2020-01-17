/*
 * Copyright (C) 2014-2020 The Project Lombok Authors.
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
package lombok.core.configuration;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.mangosdk.spi.ProviderFor;

import com.zwitserloot.cmdreader.CmdReader;
import com.zwitserloot.cmdreader.Description;
import com.zwitserloot.cmdreader.Excludes;
import com.zwitserloot.cmdreader.FullName;
import com.zwitserloot.cmdreader.InvalidCommandLineException;
import com.zwitserloot.cmdreader.Mandatory;
import com.zwitserloot.cmdreader.Requires;
import com.zwitserloot.cmdreader.Sequential;
import com.zwitserloot.cmdreader.Shorthand;

import lombok.ConfigurationKeys;
import lombok.core.LombokApp;
import lombok.core.configuration.ConfigurationParser.Collector;

@ProviderFor(LombokApp.class)
public class ConfigurationApp extends LombokApp {
	private static final URI NO_CONFIG = URI.create("");
	
	private PrintStream out = System.out;
	private PrintStream err = System.err;
	
	@Override public String getAppName() {
		return "config";
	}
	
	@Override public String getAppDescription() {
		return "Prints the configurations for the provided paths to standard out.";
	}
	
	@Override public List<String> getAppAliases() {
		return Arrays.asList("configuration", "config", "conf", "settings");
	}
	
	public static class CmdArgs {
		@Sequential
		@Mandatory(onlyIfNot={"help", "generate"})
		@Description("Paths to java files or directories the configuration is to be printed for.")
		private List<String> paths = new ArrayList<String>();
		
		@Shorthand("g")
		@Excludes("paths")
		@Description("Generates a list containing all the available configuration parameters. Add --verbose to print more information.")
		boolean generate = false;
		
		@Shorthand("v")
		@Description("Displays more information.")
		boolean verbose = false;
		
		@Shorthand("n")
		@FullName("not-mentioned")
		@Requires("verbose")
		@Description("Also display files that don't mention the key.")
		boolean notMentioned = false;
		
		@Shorthand("k")
		@Description("Limit the result to these keys.")
		private List<String> key = new ArrayList<String>();
		
		@Shorthand({"h", "?"})
		@Description("Shows this help text.")
		boolean help = false;
	}
	
	@Override public int runApp(List<String> raw) throws Exception {
		CmdReader<CmdArgs> reader = CmdReader.of(CmdArgs.class);
		CmdArgs args;
		try {
			args = reader.make(raw.toArray(new String[0]));
			if (args.help) {
				out.println(reader.generateCommandLineHelp("java -jar lombok.jar configuration"));
				return 0;
			}
		} catch (InvalidCommandLineException e) {
			err.println(e.getMessage());
			err.println(reader.generateCommandLineHelp("java -jar lombok.jar configuration"));
			return 1;
		}
		
		ConfigurationKeysLoader.LoaderLoader.loadAllConfigurationKeys();
		Collection<ConfigurationKey<?>> keys = checkKeys(args.key);
		if (keys == null) return 1;
		
		boolean verbose = args.verbose;
		if (args.generate) {
			return generate(keys, verbose, !args.key.isEmpty());
		}
		
		return display(keys, verbose, args.paths, !args.key.isEmpty(), args.notMentioned);
	}
	
	public ConfigurationApp redirectOutput(PrintStream out, PrintStream err) {
		if (out != null) this.out = out;
		if (err != null) this.err = err;
		return this;
	}
	
	public int generate(Collection<ConfigurationKey<?>> keys, boolean verbose, boolean explicit) {
		for (ConfigurationKey<?> key : keys) {
			if (!explicit && key.isHidden()) continue;
			String keyName = key.getKeyName();
			ConfigurationDataType type = key.getType();
			String description = key.getDescription();
			boolean hasDescription = description != null && !description.isEmpty();
			if (!verbose) {
				out.println(keyName);
				if (hasDescription) {
					out.print("  ");
					out.println(description);
				}
				out.println();
				continue;
			}
			out.printf("##%n## Key : %s%n## Type: %s%n", keyName, type);
			if (hasDescription) {
				out.printf("##%n## %s%n", description);
			}
			out.printf("##%n## Examples:%n#%n");
			out.printf("# clear %s%n", keyName);
			String exampleValue = type.getParser().exampleValue();
			if (type.isList()) {
				out.printf("# %s += %s%n", keyName, exampleValue);
				out.printf("# %s -= %s%n", keyName, exampleValue);
			} else {
				out.printf("# %s = %s%n", keyName, exampleValue);
			}
			out.printf("#%n%n");
		}
		if (!verbose) {
			out.println("Use --verbose for more information.");
		}
		return 0;
	}
	
	public int display(Collection<ConfigurationKey<?>> keys, boolean verbose, Collection<String> argsPaths, boolean explicitKeys, boolean notMentioned) throws Exception {
		TreeMap<URI, Set<String>> sharedDirectories = findSharedDirectories(argsPaths);
		
		if (sharedDirectories == null) return 1;
		
		Set<String> none = sharedDirectories.remove(NO_CONFIG);
		if (none != null) {
			if (none.size() == 1) {
				out.printf("No 'lombok.config' found for '%s'.%n", none.iterator().next());
			} else {
				out.println("No 'lombok.config' found for: ");
				for (String path : none) out.printf("- %s%n", path);
			}
		}
		
		final List<String> problems = new ArrayList<String>();
		ConfigurationProblemReporter reporter = new ConfigurationProblemReporter() {
			@Override public void report(String sourceDescription, String problem, int lineNumber, CharSequence line) {
				problems.add(String.format("%s: %s (%s:%d)", problem, line, sourceDescription, lineNumber));
			}
		};
		
		FileSystemSourceCache cache = new FileSystemSourceCache();
		ConfigurationParser parser = new ConfigurationParser(reporter);
		boolean first = true;
		for (Entry<URI, Set<String>> entry : sharedDirectories.entrySet()) {
			if (!first) {
				out.printf("%n%n");
			}
			Set<String> paths = entry.getValue();
			if (paths.size() == 1) {
				if (!(argsPaths.size() == 1)) out.printf("Configuration for '%s'.%n%n", paths.iterator().next());
			} else {
				out.printf("Configuration for:%n");
				for (String path : paths) out.printf("- %s%n", path);
				out.println();
			}
			URI directory = entry.getKey();
			ConfigurationResolver resolver = new BubblingConfigurationResolver(cache.forUri(directory), cache.fileToSource(parser));
			Map<ConfigurationKey<?>, ? extends Collection<String>> traces = trace(keys, directory, notMentioned);
			boolean printed = false;
			for (ConfigurationKey<?> key : keys) {
				Object value = resolver.resolve(key);
				Collection<String> modifications = traces.get(key);
				if (!modifications.isEmpty() || explicitKeys) {
					if (printed && verbose) out.println();
					printValue(key, value, verbose, modifications);
					printed = true;
				}
			}
			if (!printed) out.println("<default>");
			first = false;
		}
		
		if (!problems.isEmpty()) {
			err.printf("Problems in the configuration files:%n");
			for (String problem : problems) err.printf("- %s%n", problem);
		}
		
		return 0;
	}
	
	private void printValue(ConfigurationKey<?> key, Object value, boolean verbose, Collection<String> history) {
		if (verbose) out.printf("# %s%n", key.getDescription());
		if (value == null) {
			out.printf("clear %s%n", key.getKeyName());
		} else if (value instanceof List<?>) {
			List<?> list = (List<?>)value;
			if (list.isEmpty()) out.printf("clear %s%n", key.getKeyName());
			for (Object element : list) out.printf("%s += %s%n", key.getKeyName(), element);
		} else {
			out.printf("%s = %s%n", key.getKeyName(), value);
		}
		if (!verbose) return;
		for (String modification : history) out.printf("# %s%n", modification);
	}
	
	private static final ConfigurationProblemReporter VOID = new ConfigurationProblemReporter() {
		@Override public void report(String sourceDescription, String problem, int lineNumber, CharSequence line) {}
	};
	
	private Map<ConfigurationKey<?>, ? extends Collection<String>> trace(Collection<ConfigurationKey<?>> keys, URI directory, boolean notMentioned) throws Exception {
		Map<ConfigurationKey<?>, List<String>> result = new HashMap<ConfigurationKey<?>, List<String>>();
		for (ConfigurationKey<?> key : keys) result.put(key, new ArrayList<String>());
		Set<ConfigurationKey<?>> used = new HashSet<ConfigurationKey<?>>();
		
		boolean stopBubbling = false;
		Collection<ConfigurationFile> visited = new HashSet<ConfigurationFile>();
		for (ConfigurationFile context = ConfigurationFile.forDirectory(new File(directory)); context != null && !stopBubbling; context = context.parent()) {
			if (!context.exists()) continue;
			
			Deque<Source> round = new ArrayDeque<Source>();
			round.push(new Source(context, context.description()));
			
			while (!round.isEmpty()) {
				Source current = round.pop();
				if (current == null || !visited.add(current.file) || !current.file.exists()) continue;
				
				Map<ConfigurationKey<?>, List<String>> traces = trace(current.file, keys, round);
				
				stopBubbling = stopBubbling(traces.get(ConfigurationKeys.STOP_BUBBLING));
				for (ConfigurationKey<?> key : keys) {
					List<String> modifications = traces.get(key);
					if (modifications == null) {
						modifications = new ArrayList<String>();
						if (notMentioned) {
							modifications.add("");
							modifications.add(current.description + ":");
							modifications.add("     <'" + key.getKeyName() + "' not mentioned>");
						}
					} else {
						used.add(key);
						modifications.add(0, current.description + ":");
						modifications.add(0, "");
					}
					result.get(key).addAll(0, modifications);
				}
			}
		}
		for (ConfigurationKey<?> key : keys) {
			if (used.contains(key)) {
				List<String> modifications = result.get(key);
				modifications.remove(0);
				if (stopBubbling) {
					String mostRecent = modifications.get(0);
					modifications.set(0, mostRecent.substring(0, mostRecent.length() - 1) + " (stopped bubbling):");
				}
			} else {
				result.put(key, Collections.<String>emptyList());
			}
		}
		return result;
	}
	
	private static final class Source {
		final ConfigurationFile file;
		final String description;

		Source(ConfigurationFile file, String description) {
			this.file = file;
			this.description = description;
		}
	}
	
	private Map<ConfigurationKey<?>, List<String>> trace(ConfigurationFile context, final Collection<ConfigurationKey<?>> keys, final Deque<Source> round) throws IOException {
		final Map<ConfigurationKey<?>, List<String>> result = new HashMap<ConfigurationKey<?>, List<String>>();
		
		Collector collector = new Collector() {
			@Override public void addImport(ConfigurationFile importFile, ConfigurationFile context, int lineNumber) {
				round.push(new Source(importFile, importFile.description() + " (imported from " + context.description() + ":" + lineNumber + ")"));
			}
			@Override public void clear(ConfigurationKey<?> key, ConfigurationFile context, int lineNumber) {
				trace(key, "clear " + key.getKeyName(), lineNumber);
			}
			
			@Override public void set(ConfigurationKey<?> key, Object value, ConfigurationFile context, int lineNumber) {
				trace(key, key.getKeyName() + " = " + value, lineNumber);
			}
			
			@Override public void add(ConfigurationKey<?> key, Object value, ConfigurationFile context, int lineNumber) {
				trace(key, key.getKeyName() + " += " + value, lineNumber);
			}
			
			@Override public void remove(ConfigurationKey<?> key, Object value, ConfigurationFile context, int lineNumber) {
				trace(key, key.getKeyName() + " -= " + value, lineNumber);
			}
			
			private void trace(ConfigurationKey<?> key, String message, int lineNumber) {
				if (!keys.contains(key) && key != ConfigurationKeys.STOP_BUBBLING) return;
				List<String> traces = result.get(key);
				if (traces == null) {
					traces = new ArrayList<String>();
					result.put(key, traces);
				}
				traces.add(String.format("%4d: %s", lineNumber, message));
			}

		};
		new ConfigurationParser(VOID).parse(context, collector);
		return result;
	}
	
	private boolean stopBubbling(List<String> stops) {
		return stops != null && !stops.isEmpty() && stops.get(stops.size() -1).endsWith("true");
	}
	
	private Collection<ConfigurationKey<?>> checkKeys(List<String> keyList) {
		Map<String, ConfigurationKey<?>> registeredKeys = ConfigurationKey.registeredKeys();
		if (keyList.isEmpty()) return registeredKeys.values();
		
		Collection<ConfigurationKey<?>> keys = new ArrayList<ConfigurationKey<?>>();
		for (String keyName : keyList) {
			ConfigurationKey<?> key = registeredKeys.get(keyName);
			if (key == null) {
				err.printf("Unknown key '%s'%n", keyName);
				return null;
			}
			keys.remove(key);
			keys.add(key);
		}
		return keys;
	}
	
	private TreeMap<URI, Set<String>> findSharedDirectories(Collection<String> paths) {
		TreeMap<URI,Set<String>> sharedDirectories = new TreeMap<URI, Set<String>>(new Comparator<URI>() {
			@Override public int compare(URI o1, URI o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		for (String path : paths) {
			File file = new File(path);
			if (!file.exists()) {
				err.printf("File not found: '%s'%n", path);
				return null;
			}
			URI first = findFirstLombokDirectory(file);
			Set<String> sharedBy = sharedDirectories.get(first);
			if (sharedBy == null) {
				sharedBy = new TreeSet<String>();
				sharedDirectories.put(first, sharedBy);
			}
			sharedBy.add(path);
		}
		return sharedDirectories;
	}
	
	private URI findFirstLombokDirectory(File file) {
		File current = new File(file.toURI().normalize());
		if (file.isFile()) current = current.getParentFile();
		while (current != null) {
			if (new File(current, "lombok.config").exists()) return current.toURI();
			current = current.getParentFile();
		}
		return NO_CONFIG;
	}
}
