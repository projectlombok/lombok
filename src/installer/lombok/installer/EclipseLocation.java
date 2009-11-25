/*
 * Copyright © 2009 Reinier Zwitserloot and Roel Spilker.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Represents an Eclipse installation.
 * An instance can figure out if an Eclipse installation has been lombok-ified, and can
 * install and uninstall lombok from the Eclipse installation.
 */
final class EclipseLocation {
	private static final String OS_NEWLINE;
	
	static {
		String os = System.getProperty("os.name", "");
		
		if ("Mac OS".equals(os)) OS_NEWLINE = "\r";
		else if (os.toLowerCase().contains("windows")) OS_NEWLINE = "\r\n";
		else OS_NEWLINE = "\n";
	}
	
	private final String name;
	private final File eclipseIniPath;
	private volatile boolean hasLombok;
	
	/** Toggling the 'selected' checkbox in the GUI is tracked via this boolean */
	boolean selected = true;
	
	/**
	 * Thrown when creating a new EclipseLocation with a path object that doesn't, in fact,
	 * point at an Eclipse installation.
	 */
	static final class NotAnEclipseException extends Exception {
		private static final long serialVersionUID = 1L;
		
		public NotAnEclipseException(String message, Throwable cause) {
			super(message, cause);
		}
		
		/**
		 * Renders a message dialog with information about what went wrong.
		 */
		void showDialog(JFrame appWindow) {
			JOptionPane.showMessageDialog(appWindow, getMessage(), "Cannot configure Eclipse installation", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	private EclipseLocation(String nameOfLocation, File pathToEclipseIni) throws NotAnEclipseException {
		this.name = nameOfLocation;
		this.eclipseIniPath = pathToEclipseIni;
		try {
			this.hasLombok = checkForLombok(eclipseIniPath);
		} catch (IOException e) {
			throw new NotAnEclipseException(
					"I can't read the configuration file of the Eclipse installed at " + name + "\n" +
					"You may need to run this installer with root privileges if you want to modify that Eclipse.", e);
		}
	}
	
	private static final List<String> eclipseExecutableNames = Collections.unmodifiableList(Arrays.asList(
			"eclipse.app", "eclipse.exe", "eclipse"));
	
	/**
	 * Create a new EclipseLocation by pointing at either the directory contain the Eclipse executable, or the executable itself,
	 * or an eclipse.ini file.
	 * 
	 * @throws NotAnEclipseException
	 *             If this isn't an Eclipse executable or a directory with an
	 *             Eclipse executable.
	 */
	public static EclipseLocation create(String path) throws NotAnEclipseException {
		if (path == null) throw new NullPointerException("path");
		File p = new File(path);
		
		if (!p.exists()) throw new NotAnEclipseException("File does not exist: " + path, null);
		if (p.isDirectory()) {
			for (String possibleExeName : eclipseExecutableNames) {
				File f = new File(p, possibleExeName);
				if (f.exists()) return findEclipseIniFromExe(f, 0);
			}
			
			File f = new File(p, "eclipse.ini");
			if (f.exists()) return new EclipseLocation(getFilePath(p), f);
		}
		
		if (p.isFile()) {
			if (p.getName().equalsIgnoreCase("eclipse.ini")) {
				return new EclipseLocation(getFilePath(p.getParentFile()), p);
			}
			
			if (eclipseExecutableNames.contains(p.getName().toLowerCase())) {
				return findEclipseIniFromExe(p, 0);
			}
		}
		
		throw new NotAnEclipseException("This path does not appear to contain an Eclipse installation: " + p, null);
	}
	
	private static EclipseLocation findEclipseIniFromExe(File exePath, int loopCounter) throws NotAnEclipseException {
		/* Try looking for eclipse.ini as sibling to the executable */ {
			File ini = new File(exePath.getParentFile(), "eclipse.ini");
			if (ini.isFile()) return new EclipseLocation(getFilePath(exePath), ini);
		}
		
		/* Try looking for Eclipse/app/Contents/MacOS/eclipse.ini as sibling to executable; this works on Mac OS X. */ {
			File ini = new File(exePath.getParentFile(), "Eclipse.app/Contents/MacOS/eclipse.ini");
			if (ini.isFile()) return new EclipseLocation(getFilePath(exePath), ini);
		}
		
		/* If executable is a soft link, follow it and retry. */ {
			if (loopCounter < 50) {
				try {
					String oPath = exePath.getAbsolutePath();
					String nPath = exePath.getCanonicalPath();
					if (!oPath.equals(nPath)) try {
						return findEclipseIniFromExe(new File(nPath), loopCounter + 1);
					} catch (NotAnEclipseException ignore) {
						// Unlinking didn't help find an eclipse, so continue.
					}
				} catch (IOException ignore) { /* okay, that didn't work, assume it isn't a soft link then. */ }
			}
		}
		
		/* If executable is a linux LSB-style path, then look in the usual places that package managers like apt-get use.*/ {
			String path = exePath.getAbsolutePath();
			try {
				path = exePath.getCanonicalPath();
			} catch (IOException ignore) { /* We'll stick with getAbsolutePath()'s result then. */ }
			
			if (path.equals("/usr/bin/eclipse") || path.equals("/bin/eclipse") || path.equals("/usr/local/bin/eclipse")) {
				File ini = new File("/usr/lib/eclipse/eclipse.ini");
				if (ini.isFile()) return new EclipseLocation(path, ini);
				ini = new File("/usr/local/lib/eclipse/eclipse.ini");
				if (ini.isFile()) return new EclipseLocation(path, ini);
				ini = new File("/usr/local/etc/eclipse/eclipse.ini");
				if (ini.isFile()) return new EclipseLocation(path, ini);
				ini = new File("/etc/eclipse.ini");
				if (ini.isFile()) return new EclipseLocation(path, ini);
			}
		}
		
		/* If we get this far, we lose. */
		throw new NotAnEclipseException("This path does not appear to contain an eclipse installation: " + exePath, null);
	}
	
	public static String getFilePath(File p) {
		try {
			return p.getCanonicalPath();
		} catch (IOException e) {
			String x = p.getAbsolutePath();
			return x == null ? p.getPath() : x;
		}
	}
	
	@Override public int hashCode() {
		return eclipseIniPath.hashCode();
	}
	
	@Override public boolean equals(Object o) {
		if (!(o instanceof EclipseLocation)) return false;
		return ((EclipseLocation)o).eclipseIniPath.equals(eclipseIniPath);
	}
	
	/**
	 * Returns the name of this location; generally the path to the eclipse executable.
	 * 
	 * Executables: "eclipse.exe" (Windows), "Eclipse.app" (Mac OS X), "eclipse" (Linux and other unixes).
	 */
	String getName() {
		return name;
	}
	
	/**
	 * @return true if the Eclipse installation has been instrumented with lombok.
	 */
	boolean hasLombok() {
		return hasLombok;
	}
	
	private final Pattern JAVA_AGENT_LINE_MATCHER = Pattern.compile(
			"^\\-javaagent\\:.*lombok.*\\.jar$", Pattern.CASE_INSENSITIVE);
	
	private final Pattern BOOTCLASSPATH_LINE_MATCHER = Pattern.compile(
			"^\\-Xbootclasspath\\/a\\:(.*lombok.*\\.jar.*)$", Pattern.CASE_INSENSITIVE);
	
	private boolean checkForLombok(File iniFile) throws IOException {
		if (!iniFile.exists()) return false;
		FileInputStream fis = new FileInputStream(iniFile);
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line;
			while ((line = br.readLine()) != null) {
				if (JAVA_AGENT_LINE_MATCHER.matcher(line.trim()).matches()) return true;
			}
			
			return false;
		} finally {
			fis.close();
		}
	}
	
	/** Thrown when uninstalling lombok fails. */
	static class UninstallException extends Exception {
		private static final long serialVersionUID = 1L;
		
		public UninstallException(String message, Throwable cause) {
			super(message, cause);
		}
	}
	
	/** Returns directories that may contain lombok.jar files that need to be deleted. */
	private List<File> getUninstallDirs() {
		List<File> result = new ArrayList<File>();
		File x = new File(name);
		if (!x.isDirectory()) x = x.getParentFile();
		if (x.isDirectory()) result.add(x);
		result.add(eclipseIniPath.getParentFile());
		return result;
	}
	
	/**
	 * Uninstalls lombok from this location.
	 * It's a no-op if lombok wasn't there in the first place,
	 * and it will remove a half-succeeded lombok installation as well.
	 * 
	 * @throws UninstallException
	 *             If there's an obvious I/O problem that is preventing
	 *             installation. bugs in the uninstall code will probably throw
	 *             other exceptions; this is intentional.
	 */
	void uninstall() throws UninstallException {
		for (File dir : getUninstallDirs()) {
			File lombokJar = new File(dir, "lombok.jar");
			if (lombokJar.exists()) {
				if (!lombokJar.delete()) throw new UninstallException(
						"Can't delete " + lombokJar.getAbsolutePath() + generateWriteErrorMessage(), null);
			}
			
			/* legacy code - lombok at one point used to have a separate jar for the eclipse agent.
			 * Leave this code in to delete it for those upgrading from an old version. */ {
				File agentJar = new File(dir, "lombok.eclipse.agent.jar");
				if (agentJar.exists()) {
					if (!agentJar.delete()) throw new UninstallException(
							"Can't delete " + agentJar.getAbsolutePath() + generateWriteErrorMessage(), null);
				}
			}
		}
		
		StringBuilder newContents = new StringBuilder();
		if (eclipseIniPath.exists()) {
			try {
				FileInputStream fis = new FileInputStream(eclipseIniPath);
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
					String line;
					while ((line = br.readLine()) != null) {
						if (JAVA_AGENT_LINE_MATCHER.matcher(line).matches()) continue;
						Matcher m = BOOTCLASSPATH_LINE_MATCHER.matcher(line);
						if (m.matches()) {
							StringBuilder elemBuilder = new StringBuilder();
							elemBuilder.append("-Xbootclasspath/a:");
							boolean first = true;
							for (String elem : m.group(1).split(Pattern.quote(File.pathSeparator))) {
								if (elem.toLowerCase().endsWith("lombok.jar")) continue;
								/* legacy code -see previous comment that starts with 'legacy' */ {
									if (elem.toLowerCase().endsWith("lombok.eclipse.agent.jar")) continue;
								}
								if (first) first = false;
								else elemBuilder.append(File.pathSeparator);
								elemBuilder.append(elem);
							}
							if (!first) newContents.append(elemBuilder.toString()).append(OS_NEWLINE);
							continue;
						}
						
						newContents.append(line).append(OS_NEWLINE);
					}
					
				} finally {
					fis.close();
				}
				
				FileOutputStream fos = new FileOutputStream(eclipseIniPath);
				try {
					fos.write(newContents.toString().getBytes());
				} finally {
					fos.close();
				}
			} catch (IOException e) {
				throw new UninstallException("Cannot uninstall lombok from " + name + generateWriteErrorMessage(), e);
			}
		}
	}
	
	/** Thrown when installing lombok fails. */
	static class InstallException extends Exception {
		private static final long serialVersionUID = 1L;
		
		public InstallException(String message, Throwable cause) {
			super(message, cause);
		}
	}
	
	private static String generateWriteErrorMessage() {
		String osSpecificError;
		
		switch (EclipseFinder.getOS()) {
		default:
		case MAC_OS_X:
		case UNIX:
			osSpecificError = ":\nStart terminal, go to the directory with lombok.jar, and run: sudo java -jar lombok.jar";
			break;
		case WINDOWS:
			osSpecificError = ":\nStart a new cmd (dos box) with admin privileges, go to the directory with lombok.jar, and run: java -jar lombok.jar";
			break;
		}
		
		return ", probably because this installer does not have the access rights.\n" +
		"Try re-running the installer with administrative privileges" + osSpecificError;
	}
	
	/**
	 * Install lombok into the Eclipse at this location.
	 * If lombok is already there, it is overwritten neatly (upgrade mode).
	 * 
	 * @throws InstallException
	 *             If there's an obvious I/O problem that is preventing
	 *             installation. bugs in the install code will probably throw
	 *             other exceptions; this is intentional.
	 */
	void install() throws InstallException {
		// For whatever reason, relative paths in your eclipse.ini file don't work on linux, but only for -javaagent.
		// If someone knows how to fix this, please do so, as this current hack solution (putting the absolute path
		// to the jar files in your eclipse.ini) means you can't move your eclipse around on linux without lombok
		// breaking it. NB: rerunning lombok.jar installer and hitting 'update' will fix it if you do that.
		boolean fullPathRequired = EclipseFinder.getOS() == EclipseFinder.OS.UNIX;
		
		boolean installSucceeded = false;
		StringBuilder newContents = new StringBuilder();
		//If 'installSucceeded' is true here, something very weird is going on, but instrumenting all of them
		//is no less bad than aborting, and this situation should be rare to the point of non-existence.
		
		File lombokJar = new File(eclipseIniPath.getParentFile(), "lombok.jar");
		
		File ourJar = EclipseFinder.findOurJar();
		byte[] b = new byte[524288];
		boolean readSucceeded = true;
		try {
			FileOutputStream out = new FileOutputStream(lombokJar);
			try {
				readSucceeded = false;
				InputStream in = new FileInputStream(ourJar);
				try {
					while (true) {
						int r = in.read(b);
						if (r == -1) break;
						if (r > 0) readSucceeded = true;
						out.write(b, 0, r);
					}
				} finally {
					in.close();
				}
			} finally {
				out.close();
			}
		} catch (IOException e) {
			try {
				lombokJar.delete();
			} catch (Throwable ignore) { /* Nothing we can do about that. */ }
			if (!readSucceeded) throw new InstallException(
					"I can't read my own jar file. I think you've found a bug in this installer!\nI suggest you restart it " +
					"and use the 'what do I do' link, to manually install lombok. Also, tell us about this at:\n" +
					"http://groups.google.com/group/project-lombok - Thanks!", e);
			throw new InstallException("I can't write to your Eclipse directory at " + name + generateWriteErrorMessage(), e);
		}
		
		/* legacy - delete lombok.eclipse.agent.jar if its there, which lombok no longer uses. */ {
			new File(lombokJar.getParentFile(), "lombok.eclipse.agent.jar").delete();
		}
		
		try {
			FileInputStream fis = new FileInputStream(eclipseIniPath);
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				String line;
				while ((line = br.readLine()) != null) {
					if (JAVA_AGENT_LINE_MATCHER.matcher(line).matches()) continue;
					Matcher m = BOOTCLASSPATH_LINE_MATCHER.matcher(line);
					if (m.matches()) {
						StringBuilder elemBuilder = new StringBuilder();
						elemBuilder.append("-Xbootclasspath/a:");
						boolean first = true;
						for (String elem : m.group(1).split(Pattern.quote(File.pathSeparator))) {
							if (elem.toLowerCase().endsWith("lombok.jar")) continue;
							/* legacy code -see previous comment that starts with 'legacy' */ {
								if (elem.toLowerCase().endsWith("lombok.eclipse.agent.jar")) continue;
							}
							if (first) first = false;
							else elemBuilder.append(File.pathSeparator);
							elemBuilder.append(elem);
						}
						if (!first) newContents.append(elemBuilder.toString()).append(OS_NEWLINE);
						continue;
					}
					
					newContents.append(line).append(OS_NEWLINE);
				}
				
			} finally {
				fis.close();
			}
			
			String fullPathToLombok = fullPathRequired ? (lombokJar.getParentFile().getCanonicalPath() + File.separator) : "";
			
			newContents.append(String.format(
					"-javaagent:%slombok.jar", fullPathToLombok)).append(OS_NEWLINE);
			newContents.append(String.format(
					"-Xbootclasspath/a:%slombok.jar", fullPathToLombok)).append(OS_NEWLINE);
			
			FileOutputStream fos = new FileOutputStream(eclipseIniPath);
			try {
				fos.write(newContents.toString().getBytes());
			} finally {
				fos.close();
			}
			installSucceeded = true;
		} catch (IOException e) {
			throw new InstallException("Cannot install lombok at " + name + generateWriteErrorMessage(), e);
		} finally {
			if (!installSucceeded) try {
				lombokJar.delete();
			} catch (Throwable ignore) {}
		}
		
		if (!installSucceeded) {
			throw new InstallException("I can't find the eclipse.ini file. Is this a real Eclipse installation?", null);
		}
	}
}
