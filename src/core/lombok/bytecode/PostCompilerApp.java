/*
 * Copyright (C) 2010-2021 The Project Lombok Authors.
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.core.DiagnosticsReceiver;
import lombok.core.LombokApp;
import lombok.core.PostCompiler;
import lombok.spi.Provides;

import com.zwitserloot.cmdreader.CmdReader;
import com.zwitserloot.cmdreader.Description;
import com.zwitserloot.cmdreader.InvalidCommandLineException;
import com.zwitserloot.cmdreader.Mandatory;
import com.zwitserloot.cmdreader.Sequential;
import com.zwitserloot.cmdreader.Shorthand;

@Provides
public class PostCompilerApp extends LombokApp {
	@Override public List<String> getAppAliases() {
		return Arrays.asList("post", "postcompile");
	}
	
	@Override public String getAppDescription() {
		return "Runs registered post compiler handlers to against existing class files, modifying them in the process.";
	}
	
	@Override public String getAppName() {
		return "post-compile";
	}
	
	public static class CmdArgs {
		@Sequential
		@Mandatory
		@Description("paths to class files to be converted. If a directory is named, all files (recursively) in that directory will be converted.")
		private List<String> classFiles = new ArrayList<String>();
		
		@Shorthand("v")
		@Description("Prints lots of status information as the post compiler runs")
		boolean verbose = false;
		
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
				System.out.println(reader.generateCommandLineHelp("java -jar lombok.jar post-compile"));
				return 0;
			}
		} catch (InvalidCommandLineException e) {
			System.err.println(e.getMessage());
			System.err.println(reader.generateCommandLineHelp("java -jar lombok.jar post-compile"));
			return 1;
		}
		
		int filesVisited = 0, filesTouched = 0;
		for (File file : cmdArgsToFiles(args.classFiles)) {
			if (!file.exists() || !file.isFile()) {
				System.out.printf("Cannot find file '%s'\n", file);
				continue;
			}
			filesVisited++;
			if (args.verbose) System.out.println("Processing " + file.getAbsolutePath());
			byte[] original = readFile(file);
			byte[] clone = original.clone();
			byte[] transformed = PostCompiler.applyTransformations(clone, file.toString(), DiagnosticsReceiver.CONSOLE);
			if (clone != transformed && !Arrays.equals(original, transformed)) {
				filesTouched++;
				if (args.verbose) System.out.println("Rewriting " + file.getAbsolutePath());
				writeFile(file, transformed);
			}
		}
		
		if (args.verbose) {
			System.out.printf("Total files visited: %d total files changed: %d\n", filesVisited, filesTouched);
		}
		
		return filesVisited == 0 ? 1 : 0;
	}
	
	static List<File> cmdArgsToFiles(List<String> fileNames) {
		List<File> filesToProcess = new ArrayList<File>();
		for (String f : fileNames) addFiles(filesToProcess, f);
		return filesToProcess;
	}
	
	static void addFiles(List<File> filesToProcess, String f) {
		File file = new File(f);
		if (file.isDirectory()) {
			addRecursively(filesToProcess, file);
		} else {
			filesToProcess.add(file);
		}
	}
	
	static void addRecursively(List<File> filesToProcess, File file) {
		for (File f : file.listFiles()) {
			if (f.isDirectory()) addRecursively(filesToProcess, f);
			else if (f.getName().endsWith(".class")) filesToProcess.add(f);
		}
	}
	
	static byte[] readFile(File file) throws IOException {
		byte[] buffer = new byte[1024];
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		FileInputStream fileInputStream = new FileInputStream(file);
		try {
			while (true) {
				int read = fileInputStream.read(buffer);
				if (read == -1) break;
				bytes.write(buffer, 0, read);
			}
		} finally {
			fileInputStream.close();
		}
		return bytes.toByteArray();
	}
	
	static void writeFile(File file, byte[] transformed) throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		try {
			out.write(transformed);
		} finally {
			out.close();
		}
	}
}
