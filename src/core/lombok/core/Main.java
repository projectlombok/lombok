/*
 * Copyright (C) 2009-2021 The Project Lombok Authors.
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
package lombok.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.spi.Provides;

public class Main {
	private static final Collection<?> HELP_SWITCHES = Collections.unmodifiableList(Arrays.asList(
			"/?", "/h", "/help", "-h", "-help", "--help", "help", "h"
	));
	
	public static void main(String[] args) throws IOException {
		Thread.currentThread().setContextClassLoader(Main.class.getClassLoader());
		int err = new Main(SpiLoadUtil.readAllFromIterator(
				SpiLoadUtil.findServices(LombokApp.class)), Arrays.asList(args)).go();
		if (err != 0) {
			System.exit(err);
		}
	}
	
	@Provides
	public static class VersionApp extends LombokApp {
		@Override public String getAppName() {
			return "version";
		}
		
		@Override public String getAppDescription() {
			return "prints lombok's version.";
		}
		
		@Override public List<String> getAppAliases() {
			return Arrays.asList("-version", "--version");
		}
		
		@Override public int runApp(List<String> args) {
			System.out.println(Version.getFullVersion());
			return 0;
		}
	}
	
	@Provides
	public static class LicenseApp extends LombokApp {
		@Override public String getAppName() {
			return "license";
		}
		
		@Override public String getAppDescription() {
			return "prints license information.";
		}
		
		@Override public List<String> getAppAliases() {
			return Arrays.asList("licence", "copyright", "copyleft", "gpl");
		}
		
		@Override public int runApp(List<String> args) {
			try {
				InputStream in = Main.class.getResourceAsStream("/LICENSE");
				try {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte[] b = new byte[65536];
					while (true) {
						int r = in.read(b);
						if (r == -1) break;
						out.write(b, 0, r);
					}
					System.out.println(new String(out.toByteArray()));
					return 0;
				} finally {
					in.close();
				}
			} catch (Exception e) {
				System.err.println("License file not found. Check https://projectlombok.org/LICENSE");
				return 1;
			}
		}
	}
	
	private final List<LombokApp> apps;
	private final List<String> args;
	
	public Main(List<LombokApp> apps, List<String> args) {
		this.apps = apps;
		this.args = args;
	}
	
	public int go() {
		if (!args.isEmpty() && HELP_SWITCHES.contains(args.get(0))) {
			printHelp(null, System.out);
			return 0;
		}
		
		String command = args.isEmpty() ? "" : args.get(0).trim();
		if (command.startsWith("--")) command = command.substring(2);
		else if (command.startsWith("-")) command = command.substring(1);
		
		List<String> subArgs = args.isEmpty() ? Collections.<String>emptyList() : Collections.unmodifiableList(
				args.subList(1, args.size()));
		
		for (LombokApp app : apps) {
			if (app.getAppName().equals(command) || app.getAppAliases().contains(command)) {
				try {
					return app.runApp(subArgs);
				} catch (Exception e) {
					e.printStackTrace();
					return 5;
				}
			}
		}
		
		printHelp("Unknown command: " + command, System.err);
		return 1;
	}
	
	public void printHelp(String message, PrintStream out) {
		if (message != null) {
			out.println(message);
			out.println("------------------------------");
		}
		out.println("projectlombok.org " + Version.getFullVersion());
		out.println("Copyright (C) 2009-2021 The Project Lombok Authors.");
		out.println("Run 'lombok license' to see the lombok license agreement.");
		out.println();
		out.println("Run lombok without any parameters to start the graphical installer.");
		out.println("Other available commands:");
		for (LombokApp app : apps) {
			if (app.isDebugTool()) continue;
			String[] desc = app.getAppDescription().split("\n");
			for (int i = 0; i < desc.length; i++) {
				out.printf("  %15s    %s\n", i == 0 ? app.getAppName() : "", desc[i]);
			}
		}
		out.println();
		out.println("Run lombok commandName --help for more info on each command.");
	}
}
