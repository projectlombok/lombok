/*
 * Copyright (C) 2012-2021 The Project Lombok Authors.
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
package lombok.bytecode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lombok.core.LombokApp;
import lombok.spi.Provides;

import com.zwitserloot.cmdreader.CmdReader;
import com.zwitserloot.cmdreader.Description;
import com.zwitserloot.cmdreader.InvalidCommandLineException;
import com.zwitserloot.cmdreader.Mandatory;
import com.zwitserloot.cmdreader.Sequential;
import com.zwitserloot.cmdreader.Shorthand;

@Provides
public class PoolConstantsApp extends LombokApp {
	@Override public String getAppName() {
		return "Xprintpool";
	}
	
	@Override public String getAppDescription() {
		return "Prints the content of the constant pool to standard out.";
	}
	
	@Override public boolean isDebugTool() {
		return true;
	}
	
	public static class CmdArgs {
		@Sequential
		@Mandatory
		@Description("paths to class files to be printed. If a directory is named, all files (recursively) in that directory will be printed.")
		private List<String> classFiles = new ArrayList<String>();
		
		@Shorthand({"h", "?"})
		@Description("Shows this help text")
		boolean help = false;
	}
	
	@Override public int runApp(List<String> raw) throws Exception {
		CmdReader<CmdArgs> reader = CmdReader.of(CmdArgs.class);
		CmdArgs args;
		try {
			args = reader.make(raw.toArray(new String[0]));
			if (args.help) {
				System.out.println(reader.generateCommandLineHelp("java -jar lombok.jar -printpool"));
				return 0;
			}
		} catch (InvalidCommandLineException e) {
			System.err.println(e.getMessage());
			System.err.println(reader.generateCommandLineHelp("java -jar lombok.jar -printpool"));
			return 1;
		}
		
		List<File> filesToProcess = PostCompilerApp.cmdArgsToFiles(args.classFiles);
		int filesVisited = 0;
		boolean moreThanOne = filesToProcess.size() > 1;
		for (File file : filesToProcess) {
			if (!file.exists() || !file.isFile()) {
				System.out.printf("Cannot find file '%s'\n", file.getAbsolutePath());
				continue;
			}
			filesVisited++;
			if (moreThanOne) System.out.printf("Processing '%s'\n", file.getAbsolutePath());
			System.out.println(new ClassFileMetaData(PostCompilerApp.readFile(file)).poolContent());
		}
		
		if (moreThanOne) System.out.printf("Total files visited: %d\n", filesVisited);
		
		return filesVisited == 0 ? 1 : 0;
	}
}
