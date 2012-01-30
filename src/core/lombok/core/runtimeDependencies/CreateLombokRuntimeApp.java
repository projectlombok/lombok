/*
 * Copyright (C) 2009-2011 The Project Lombok Authors.
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
package lombok.core.runtimeDependencies;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import lombok.core.LombokApp;
import lombok.core.SpiLoadUtil;

import org.mangosdk.spi.ProviderFor;

import com.zwitserloot.cmdreader.CmdReader;
import com.zwitserloot.cmdreader.Description;
import com.zwitserloot.cmdreader.InvalidCommandLineException;
import com.zwitserloot.cmdreader.Mandatory;
import com.zwitserloot.cmdreader.Requires;
import com.zwitserloot.cmdreader.Shorthand;

@ProviderFor(LombokApp.class)
public class CreateLombokRuntimeApp extends LombokApp {
	private List<RuntimeDependencyInfo> infoObjects;
	
	@Override public String getAppName() {
		return "createRuntime";
	}
	
	@Override public String getAppDescription() {
		return "Creates a small lombok-runtime.jar with the runtime\n" +
				"dependencies of all lombok transformations that have them,\n" +
				"and prints the names of each lombok transformation that\n" +
				"requires the lombok-runtime.jar at runtime.";
	}
	
	@Override public List<String> getAppAliases() {
		return Arrays.asList("runtime");
	}
	
	private static class CmdArgs {
		@Shorthand("p")
		@Description("Prints those lombok transformations that require lombok-runtime.jar.")
		@Mandatory(onlyIfNot="create")
		boolean print;
		
		@Shorthand("c")
		@Description("Creates the lombok-runtime.jar.")
		@Mandatory(onlyIfNot="print")
		boolean create;
		
		@Shorthand("o")
		@Description("Where to write the lombok-runtime.jar. Defaults to the current working directory.")
		@Requires("create")
		String output;
		
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
		
		initializeInfoObjects();
		
		if (args.print) {
			printRuntimeDependents();
		}
		
		int errCode = 0;
		
		if (args.create) {
			File out = new File("./lombok-runtime.jar");
			if (args.output != null) {
				out = new File(args.output);
				if (out.isDirectory()) out = new File(out, "lombok-runtime.jar");
			}
			
			try {
				errCode = writeRuntimeJar(out);
			} catch (Exception e) {
				System.err.println("ERROR: Creating " + canonical(out) + " failed: ");
				e.printStackTrace();
				return 1;
			}
		}
		
		return errCode;
	}
	
	private void printRuntimeDependents() {
		List<String> descriptions = new ArrayList<String>();
		for (RuntimeDependencyInfo info : infoObjects) descriptions.addAll(info.getRuntimeDependentsDescriptions());
		if (descriptions.isEmpty()) {
			System.out.println("Not printing dependents: No lombok transformations currently have any runtime dependencies!");
		} else {
			System.out.println("Using any of these lombok features means your app will need lombok-runtime.jar:");
			for (String desc : descriptions) {
				System.out.println(desc);
			}
		}
	}
	
	private int writeRuntimeJar(File outFile) throws Exception {
		Map<String, Class<?>> deps = new LinkedHashMap<String, Class<?>>();
		for (RuntimeDependencyInfo info : infoObjects) {
			List<String> depNames = info.getRuntimeDependencies();
			if (depNames != null) for (String depName : depNames) {
				if (!deps.containsKey(depName)) deps.put(depName, info.getClass());
			}
		}
		
		if (deps.isEmpty()) {
			System.out.println("Not generating lombok-runtime.jar: No lombok transformations currently have any runtime dependencies!");
			return 1;
		}
		
		OutputStream out = new FileOutputStream(outFile);
		boolean success = false;
		try {
			JarOutputStream jar = new JarOutputStream(out);
			deps.put("LICENSE", CreateLombokRuntimeApp.class);
			deps.put("AUTHORS", CreateLombokRuntimeApp.class);
			for (Entry<String, Class<?>> dep : deps.entrySet()) {
				InputStream in = dep.getValue().getResourceAsStream("/" + dep.getKey());
				try {
					if (in == null) {
						throw new Fail(String.format("Dependency %s contributed by %s cannot be found", dep.getKey(), dep.getValue()));
					}
					writeIntoJar(jar, dep.getKey(), in);
				} finally {
					if (in != null) in.close();
				}
			}
			jar.close();
			out.close();
			
			System.out.println("Successfully created: " + canonical(outFile));
			
			return 0;
		} catch (Throwable t) {
			try { out.close();} catch (Throwable ignore) {}
			if (!success) outFile.delete();
			if (t instanceof Fail) {
				System.err.println(t.getMessage());
				return 1;
			} else if (t instanceof Exception) {
				throw (Exception)t;
			} else if (t instanceof Error) {
				throw (Error)t;
			} else {
				throw new Exception(t);
			}
		}
	}
	
	private void writeIntoJar(JarOutputStream jar, String depName, InputStream in) throws IOException {
		jar.putNextEntry(new ZipEntry(depName));
		byte[] b = new byte[65536];
		while (true) {
			int r = in.read(b);
			if (r == -1) break;
			jar.write(b, 0, r);
		}
		jar.closeEntry();
		in.close();
	}
	
	private static class Fail extends Exception {
		Fail(String message) {
			super(message);
		}
	}
	
	private void initializeInfoObjects() throws IOException {
		infoObjects = SpiLoadUtil.readAllFromIterator(
				SpiLoadUtil.findServices(RuntimeDependencyInfo.class));
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
		out.println(reader.generateCommandLineHelp("java -jar lombok.jar createRuntime"));
	}
}
