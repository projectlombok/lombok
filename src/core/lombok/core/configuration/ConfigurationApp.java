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
package lombok.core.configuration;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.core.LombokApp;

import org.mangosdk.spi.ProviderFor;

import com.zwitserloot.cmdreader.CmdReader;
import com.zwitserloot.cmdreader.Description;
import com.zwitserloot.cmdreader.Excludes;
import com.zwitserloot.cmdreader.InvalidCommandLineException;
import com.zwitserloot.cmdreader.Mandatory;
import com.zwitserloot.cmdreader.Sequential;
import com.zwitserloot.cmdreader.Shorthand;

@ProviderFor(LombokApp.class)
public class ConfigurationApp extends LombokApp {
	
	@Override public String getAppName() {
		return "configuration";
	}
	
	@Override public String getAppDescription() {
		return "Prints the configurations for the provided paths to standard out.";
	}
	
	@Override public List<String> getAppAliases() {
		return Arrays.asList("configuration", "config");
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
				System.out.println(reader.generateCommandLineHelp("java -jar lombok.jar configuration"));
				return 0;
			}
		} catch (InvalidCommandLineException e) {
			System.err.println(e.getMessage());
			System.err.println(reader.generateCommandLineHelp("java -jar lombok.jar configuration"));
			return 1;
		}
		
		ConfigurationKeysLoader.LoaderLoader.loadAllConfigurationKeys();
		
		if (args.generate) {
			printConfiguration(System.out, args.verbose);
		} else {
			System.out.println("Not generating anything...");
		}
			
		
//		List<File> filesToProcess = PostCompilerApp.cmdArgsToFiles(args.classFiles);
		int filesVisited = 0;
//		boolean moreThanOne = filesToProcess.size() > 1;
//		for (File file : filesToProcess) {
//			if (!file.exists() || !file.isFile()) {
//				System.out.printf("Cannot find file '%s'\n", file.getAbsolutePath());
//				continue;
//			}
//			filesVisited++;
//			if (moreThanOne) System.out.printf("Processing '%s'\n", file.getAbsolutePath());
//			System.out.println(new ClassFileMetaData(PostCompilerApp.readFile(file)).poolContent());
//		}
//		
//		if (moreThanOne) System.out.printf("Total files visited: %d\n", filesVisited);
		
		return filesVisited == 0 ? 1 : 0;
	}
	
	private void printConfiguration(PrintStream out, boolean verbose) {
		for (ConfigurationKey<?> key : ConfigurationKey.registeredKeys()) {
			String keyName = key.getKeyName();
			ConfigurationDataType type = key.getType();
			if (!verbose) {
				out.printf("# %s (%s)\n", keyName, type);
				continue;
			}
			out.printf("### Key %s type %s\n", keyName, type);
			out.printf("#clear %s\n", keyName);
			if (type.isList()) {
				out.printf("#%s += %s\n", keyName, exampleValue(type));
				out.printf("#%s -= %s\n", keyName, exampleValue(type));
			} else {
				out.printf("#%s = %s\n", keyName, exampleValue(type));
			}
			out.println();
		}
	}

	private String exampleValue(ConfigurationDataType type) {
		return type.getParser().exampleValue();
	}
}
