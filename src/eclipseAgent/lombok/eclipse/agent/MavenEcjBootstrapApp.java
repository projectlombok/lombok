/*
 * Copyright (C) 2022 The Project Lombok Authors.
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
package lombok.eclipse.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import com.zwitserloot.cmdreader.CmdReader;
import com.zwitserloot.cmdreader.Description;
import com.zwitserloot.cmdreader.InvalidCommandLineException;
import com.zwitserloot.cmdreader.Shorthand;

import lombok.core.LombokApp;
import lombok.spi.Provides;

@Provides
public class MavenEcjBootstrapApp extends LombokApp {
	@Override public String getAppName() {
		return "createMavenECJBootstrap";
	}
	
	@Override public String getAppDescription() {
		return "Creates .mvn/jvm.config and .mvn/lombok-bootstrap.jar for\n" +
			"use with the ECJ compiler.";
	}
	
	private static class CmdArgs {
		@Shorthand("w")
		@Description("Overwrite existing files. Defaults to false.")
		boolean overwrite = false;
		
		@Shorthand("o")
		@Description("The root of a Maven project. Defaults to the current working directory.")
		String output;
		
		@Shorthand({"h", "?"})
		@Description("Shows this help text")
		boolean help;
	}
	
	@Override public int runApp(List<String> rawArgs) throws Exception {
		CmdReader<CmdArgs> reader = CmdReader.of(CmdArgs.class);
		CmdArgs args;
		try {
			args = reader.make(rawArgs.toArray(new String[0]));
		} catch (InvalidCommandLineException e) {
			printHelp(reader, e.getMessage(), System.err);
			return 1;
		}
		
		if (args.help) {
			printHelp(reader, null, System.out);
			return 0;
		}
		
		return createBootstrap(args.output, args.overwrite);
	}
	
	private int createBootstrap(String root, boolean overwrite) {
		File mvn = new File(root, ".mvn");
		int result = 0;
		if (result == 0) result = makeMvn(mvn);
		if (result == 0) result = makeJvmConfig(mvn, overwrite);
		if (result == 0) result = makeJar(mvn, overwrite);
		return result;
	}
	
	private int makeMvn(File mvn) {
		int result = 0;
		Exception err = null;
		try {
			if (!mvn.exists() && !mvn.mkdirs()) result = 1;
		} catch (Exception e) {
			result = 1;
			err = e;
		}
		if (result != 0) {
			System.err.println("Could not create " + mvn.getPath());
			if (err != null) err.printStackTrace(System.err);
		}
		return result;
	}
	
	private int makeJvmConfig(File mvn, boolean overwrite) {
		File jvmConfig = new File(mvn, "jvm.config");
		if (jvmConfig.exists() && !overwrite) {
			System.err.println(canonical(jvmConfig) + " exists but '-w' not specified.");
			return 1;
		}
		try {
			FileWriter writer = new FileWriter(jvmConfig);
			writer.write("-javaagent:.mvn/lombok-bootstrap.jar");
			writer.flush();
			writer.close();
			System.out.println("Successfully created: " + canonical(jvmConfig));
			return 0;
		} catch (Exception e) {
			System.err.println("Could not create: " + canonical(jvmConfig));
			e.printStackTrace(System.err);
			return 1;
		}
	}
	
	private int makeJar(File mvn, boolean overwrite) {
		File jar = new File(mvn, "lombok-bootstrap.jar");
		if (jar.exists() && !overwrite) {
			System.err.println(canonical(jar) + " but '-w' not specified.");
			return 1;
		}
		try {
			InputStream input = MavenEcjBootstrapApp.class.getResourceAsStream("/lombok/launch/mavenEcjBootstrapAgent.jar");
			FileOutputStream output = new FileOutputStream(jar);
			try {
				byte[] buffer = new byte[4096];
				int length;
				while ((length = input.read(buffer)) > 0) output.write(buffer, 0, length);
				output.flush();
				output.close();
				System.out.println("Successfully created: " + canonical(jar));
				return 0;
			} finally {
				try {
					output.close();
				} catch (Exception ignore) {}
			}
		} catch (Exception e) {
			System.err.println("Could not create: " + canonical(jar));
			e.printStackTrace(System.err);
			return 1;
		}
	}
	
	private static String canonical(File out) {
		try {
			return out.getCanonicalPath();
		} catch (Exception e) {
			return out.getAbsolutePath();
		}
	}
	
	private void printHelp(CmdReader<CmdArgs> reader, String message, PrintStream out) {
		if (message != null) {
			out.println(message);
			out.println("----------------------------");
		}
		out.println(reader.generateCommandLineHelp("java -jar lombok.jar createMavenECJBootstrap"));
	}
}
