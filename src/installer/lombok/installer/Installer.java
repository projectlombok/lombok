/*
 * Copyright (C) 2009-2016 The Project Lombok Authors.
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
package lombok.installer;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import lombok.Lombok;
import lombok.core.LombokApp;
import lombok.core.SpiLoadUtil;
import lombok.core.Version;
import lombok.installer.OsUtils.OS;
import lombok.patcher.ClassRootFinder;

import org.mangosdk.spi.ProviderFor;

import com.zwitserloot.cmdreader.CmdReader;
import com.zwitserloot.cmdreader.Description;
import com.zwitserloot.cmdreader.InvalidCommandLineException;
import com.zwitserloot.cmdreader.Sequential;
import com.zwitserloot.cmdreader.Shorthand;

/**
 * The lombok installer proper.
 * Uses swing to show a simple GUI that can add and remove the java agent to Eclipse installations.
 * Also offers info on what this installer does in case people want to instrument their Eclipse manually,
 * and looks in some common places on Mac OS X, Linux and Windows.
 */
public class Installer {
	static final URI ABOUT_LOMBOK_URL = URI.create("https://projectlombok.org");
	static final List<IdeLocationProvider> locationProviders;
	
	static {
		List<IdeLocationProvider> list = new ArrayList<IdeLocationProvider>();
		try {
			for (IdeLocationProvider provider : SpiLoadUtil.findServices(IdeLocationProvider.class)) {
				list.add(provider);
			}
		} catch (IOException e) {
			throw Lombok.sneakyThrow(e);
		}
		locationProviders = Collections.unmodifiableList(list);
	}
	
	static List<Pattern> getIdeExecutableNames() {
		List<Pattern> list = new ArrayList<Pattern>();
		for (IdeLocationProvider provider : locationProviders) {
			Pattern p = provider.getLocationSelectors();
			if (p != null) list.add(p);
		}
		return list;
	}
	
	static IdeLocation tryAllProviders(String location) throws CorruptedIdeLocationException {
		for (IdeLocationProvider provider : locationProviders) {
			IdeLocation loc = provider.create(location);
			if (loc != null) return loc;
		}
		
		return null;
	}
	
	static void autoDiscover(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems) {
		for (IdeLocationProvider provider : locationProviders) {
			provider.findIdes(locations, problems);
		}
	}
	
	public static boolean isSelf(String jar) {
		String self = ClassRootFinder.findClassRootOfClass(Installer.class);
		if (self == null) return false;
		File a = new File(jar).getAbsoluteFile();
		File b = new File(self).getAbsoluteFile();
		try { a = a.getCanonicalFile(); } catch (IOException ignore) {}
		try { b = b.getCanonicalFile(); } catch (IOException ignore) {}
		return a.equals(b);
	}
	
	@ProviderFor(LombokApp.class)
	public static class GraphicalInstallerApp extends LombokApp {
		@Override public String getAppName() {
			return "installer";
		}
		
		@Override public String getAppDescription() {
			return "Runs the graphical installer tool (default).";
		}
		
		@Override public List<String> getAppAliases() {
			return Arrays.asList("");
		}
		
		@Override public int runApp(List<String> args) throws Exception {
			return guiInstaller();
		}
	}
	
	@ProviderFor(LombokApp.class)
	public static class CommandLineInstallerApp extends LombokApp {
		@Override public String getAppName() {
			return "install";
		}
		
		@Override public String getAppDescription() {
			return "Runs the 'handsfree' command line scriptable installer.";
		}
		
		@Override public int runApp(List<String> args) throws Exception {
			return cliInstaller(false, args);
		}
	}
	
	@ProviderFor(LombokApp.class)
	public static class CommandLineUninstallerApp extends LombokApp {
		@Override public String getAppName() {
			return "uninstall";
		}
		
		@Override public String getAppDescription() {
			return "Runs the 'handsfree' command line scriptable uninstaller.";
		}
		
		@Override public int runApp(List<String> args) throws Exception {
			return cliInstaller(true, args);
		}
	}
	
	private static int guiInstaller() {
		if (OsUtils.getOS() == OS.MAC_OS_X) {
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Lombok Installer");
			System.setProperty("com.apple.macos.use-file-dialog-packages", "true");
		}
		
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						try {
							UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
						} catch (Exception ignore) {}
						
						new InstallerGUI().show();
					} catch (HeadlessException e) {
						printHeadlessInfo();
					}
				}
			});
			
			synchronized (InstallerGUI.exitMarker) {
				while (!Thread.interrupted() && InstallerGUI.exitMarker.get() == null) {
					try {
						InstallerGUI.exitMarker.wait();
					} catch (InterruptedException e) {
						return 1;
					}
				}
				Integer errCode = InstallerGUI.exitMarker.get();
				return errCode == null ? 1 : errCode;
			}
		} catch (HeadlessException e) {
			printHeadlessInfo();
			return 1;
		}
	}
	
	private static class CmdArgs {
		@Description("Specify paths to a location to install/uninstall. Use 'auto' to apply to all automatically discoverable installations.")
		@Sequential
		List<String> path = new ArrayList<String>();
		
		@Shorthand({"h", "?"})
		@Description("Shows this help text")
		boolean help;
	}
	
	public static int cliInstaller(boolean uninstall, List<String> rawArgs) {
		CmdReader<CmdArgs> reader = CmdReader.of(CmdArgs.class);
		CmdArgs args;
		try {
			args = reader.make(rawArgs.toArray(new String[0]));
		} catch (InvalidCommandLineException e) {
			System.err.println(e.getMessage());
			System.err.println("--------------------------");
			System.err.println(generateCliHelp(uninstall, reader));
			return 1;
		}
		
		if (args.help) {
			System.out.println(generateCliHelp(uninstall, reader));
			return 0;
		}
		
		if (args.path.isEmpty()) {
			System.err.println("ERROR: Nothing to do!");
			System.err.println("--------------------------");
			System.err.println(generateCliHelp(uninstall, reader));
			return 1;
		}
		
		final List<IdeLocation> locations = new ArrayList<IdeLocation>();
		final List<CorruptedIdeLocationException> problems = new ArrayList<CorruptedIdeLocationException>();
		
		if (args.path.contains("auto")) autoDiscover(locations, problems);
		
		for (String rawPath : args.path) {
			if (!rawPath.equals("auto")) {
				try {
					IdeLocation loc = tryAllProviders(rawPath);
					if (loc != null) locations.add(loc);
					else problems.add(new CorruptedIdeLocationException("Can't find any IDE at: " + rawPath, null, null));
				} catch (CorruptedIdeLocationException e) {
					problems.add(e);
				}
			}
		}
		
		int validLocations = locations.size();
		for (IdeLocation loc : locations) {
			try {
				if (uninstall) {
					loc.uninstall();
				} else {
					loc.install();
				}
				System.out.printf("Lombok %s %s: %s\n", uninstall ? "uninstalled" : "installed", uninstall ? "from" : "to", loc.getName());
			} catch (InstallException e) {
				if (e.isWarning()) {
					System.err.printf("Warning while installing at %s:\n", loc.getName());
				} else {
					System.err.printf("Installation at %s failed:\n", loc.getName());
					validLocations--;
				}
				System.err.println(e.getMessage());
			} catch (UninstallException e) {
				if (e.isWarning()) {
					System.err.printf("Warning while uninstalling at %s:\n", loc.getName());
				} else {
					System.err.printf("Uninstall at %s failed:\n", loc.getName());
					validLocations--;
				}
				System.err.println(e.getMessage());
			}
		}
		
		for (CorruptedIdeLocationException problem : problems) {
			System.err.println("WARNING: " + problem.getMessage());
		}
		
		if (validLocations == 0) {
			System.err.println("WARNING: Zero valid locations found; so nothing was done!");
		}
		return 0;
	}
	
	private static String generateCliHelp(boolean uninstall, CmdReader<CmdArgs> reader) {
		return reader.generateCommandLineHelp("java -jar lombok.jar " + (uninstall ? "uninstall" : "install"));
	}
	
	/**
	 * If run in headless mode, the installer can't show its fancy GUI. There's little point in running
	 * the installer without a GUI environment, as Eclipse doesn't run in headless mode either, so
	 * we'll make do with showing some basic info on Lombok as well as instructions for using lombok with javac.
	 */
	private static void printHeadlessInfo() {
		System.out.printf("About lombok v%s\n" +
				"Lombok makes java better by providing very spicy additions to the Java programming language," +
				"such as using @Getter to automatically generate a getter method for any field.\n\n" +
				"Browse to %s for more information. To install lombok on Eclipse, re-run this jar file on a " +
				"graphical computer system - this message is being shown because your terminal is not graphics capable.\n" +
				"Alternatively, use the command line installer (java -jar lombok.jar install --help).\n" +
				"If you are just using 'javac' or a tool that calls on javac, no installation is neccessary; just " +
				"make sure lombok.jar is in the classpath when you compile. Example:\n\n" +
				"   java -cp lombok.jar MyCode.java\n",
				Version.getVersion(), ABOUT_LOMBOK_URL);
	}
}
