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
	
	private final File path;
	private volatile boolean hasLombok;
	
	/** Toggling the 'selected' checkbox in the GUI is tracked via this boolean */
	boolean selected = true;
	
	/**
	 * Thrown when creating a new EclipseLocation with a path object that doesn't, in fact,
	 * point at an Eclipse installation.
	 */
	final class NotAnEclipseException extends Exception {
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
	
	/**
	 * Create a new EclipseLocation by pointing at either the directory contain the Eclipse executable, or the executable itself.
	 * 
	 * @throws NotAnEclipseException If this isn't an Eclipse executable or a directory with an Eclipse executable.
	 */
	EclipseLocation(String path) throws NotAnEclipseException {
		if (path == null) throw new NullPointerException("path");
		File p = new File(path);
		if (!p.exists()) throw new NotAnEclipseException("File does not exist: " + path, null);
		
		final String execName = EclipseFinder.getEclipseExecutableName();
		if (p.isDirectory()) {
			for (File f : p.listFiles()) {
				if (f.getName().equalsIgnoreCase(execName)) {
					p = f;
					break;
				}
			}
		}
		
		if (!p.exists() || !p.getName().equalsIgnoreCase(execName)) {
			throw new NotAnEclipseException("This path does not appear to contain an Eclipse installation: " + p, null);
		}
		
		this.path = p;
		try {
			this.hasLombok = checkForLombok();
		} catch (IOException e) {
			throw new NotAnEclipseException(
					"I can't read the configuration file of the Eclipse installed at " + this.path.getAbsolutePath() + "\n" +
					"You may need to run this installer with root privileges if you want to modify that Eclipse.", e);
		}
	}
	
	@Override public int hashCode() {
		return path.hashCode();
	}
	
	@Override public boolean equals(Object o) {
		if (!(o instanceof EclipseLocation)) return false;
		return ((EclipseLocation)o).path.equals(path);
	}
	
	/**
	 * Returns the absolute path to the Eclipse executable.
	 * 
	 * Executables: "eclipse.exe" (Windows), "Eclipse.app" (Mac OS X), "eclipse" (Linux and other unixes).
	 */
	String getPath() {
		return path.getAbsolutePath();
	}
	
	/**
	 * @return true if the Eclipse installation has been instrumented with lombok.
	 */
	boolean hasLombok() {
		return hasLombok;
	}
	
	/**
	 * Returns the various directories that can contain the 'eclipse.ini' file.
	 * Returns multiple directories because there are a few different ways Eclipse is packaged.
	 */
	private List<File> getTargetDirs() {
		return Arrays.asList(path.getParentFile(), new File(new File(path, "Contents"), "MacOS"));
	}
	
	private boolean checkForLombok() throws IOException {
		for (File targetDir : getTargetDirs()) {
			if (checkForLombok0(targetDir)) return true;
		}
		
		return false;
	}
	
	private final Pattern JAVA_AGENT_LINE_MATCHER = Pattern.compile(
			"^\\-javaagent\\:.*lombok.*\\.jar$", Pattern.CASE_INSENSITIVE);
	
	private final Pattern BOOTCLASSPATH_LINE_MATCHER = Pattern.compile(
	"^\\-Xbootclasspath\\/a\\:(.*lombok.*\\.jar.*)$", Pattern.CASE_INSENSITIVE);
	
	private boolean checkForLombok0(File dir) throws IOException {
		File iniFile = new File(dir, "eclipse.ini");
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
	class UninstallException extends Exception {
		private static final long serialVersionUID = 1L;
		
		public UninstallException(String message, Throwable cause) {
			super(message, cause);
		}
	}
	
	/**
	 * Uninstalls lombok from this location.
	 * It's a no-op if lombok wasn't there in the first place,
	 * and it will remove a half-succeeded lombok installation as well.
	 * 
	 * @throws UninstallException If there's an obvious I/O problem that is preventing installation.
	 *   bugs in the uninstall code will probably throw other exceptions; this is intentional.
	 */
	void uninstall() throws UninstallException {
		for (File dir : getTargetDirs()) {
			File lombokJar = new File(dir, "lombok.jar");
			if (lombokJar.exists()) {
				if (!lombokJar.delete()) throw new UninstallException(
						"Can't delete " + lombokJar.getAbsolutePath() +
						" - perhaps the installer does not have the access rights to do so.",
						null);
			}
			
			/* legacy code - lombok at one point used to have a separate jar for the eclipse agent.
			 * Leave this code in to delete it for those upgrading from an old version. */ {
				File agentJar = new File(dir, "lombok.eclipse.agent.jar");
				if (agentJar.exists()) {
					if (!agentJar.delete()) throw new UninstallException(
							"Can't delete " + agentJar.getAbsolutePath() +
							" - perhaps the installer does not have the access rights to do so.",
							null);
				}
			}
			
			File iniFile = new File(dir, "eclipse.ini");
			StringBuilder newContents = new StringBuilder();
			if (iniFile.exists()) {
				try {
					FileInputStream fis = new FileInputStream(iniFile);
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
					
					FileOutputStream fos = new FileOutputStream(iniFile);
					try {
						fos.write(newContents.toString().getBytes());
					} finally {
						fos.close();
					}
				} catch (IOException e) {
					throw new UninstallException("Cannot uninstall lombok from " + path.getAbsolutePath() +
							" probably because this installer does not have the access rights to do so.", e);
				}
			}
		}
	}
	
	/** Thrown when installing lombok fails. */
	class InstallException extends Exception {
		private static final long serialVersionUID = 1L;
		
		public InstallException(String message, Throwable cause) {
			super(message, cause);
		}
	}
	
	/**
	 * Install lombok into the Eclipse at this location.
	 * If lombok is already there, it is overwritten neatly (upgrade mode).
	 * 
	 * @throws InstallException If there's an obvious I/O problem that is preventing installation.
	 *   bugs in the install code will probably throw other exceptions; this is intentional.
	 */
	void install() throws InstallException {
		List<File> failedDirs = new ArrayList<File>();
		
		// For whatever reason, relative paths in your eclipse.ini file don't work on linux, but only for -javaagent.
		// If someone knows how to fix this, please do so, as this current hack solution (putting the absolute path
		// to the jar files in your eclipse.ini) means you can't move your eclipse around on linux without lombok
		// breaking it. NB: rerunning lombok.jar installer and hitting 'update' will fix it if you do that.
		boolean fullPathRequired = EclipseFinder.getOS() == EclipseFinder.OS.UNIX;
		
		boolean installSucceeded = false;
		for (File dir : getTargetDirs()) {
			File iniFile = new File(dir, "eclipse.ini");
			StringBuilder newContents = new StringBuilder();
			if (!iniFile.exists()) failedDirs.add(dir);
			else {
				//If 'installSucceeded' is true here, something very weird is going on, but instrumenting all of them
				//is no less bad than aborting, and this situation should be rare to the point of non-existence.
				
				File lombokJar = new File(iniFile.getParentFile(), "lombok.jar");
				
				File ourJar = EclipseFinder.findOurJar();
				byte[] b = new byte[524288];
				boolean readSucceeded = false;
				try {
					FileOutputStream out = new FileOutputStream(lombokJar);
					InputStream in = new FileInputStream(ourJar);
					try {
						while (true) {
							int r = in.read(b);
							if (r == -1) break;
							out.write(b, 0, r);
						}
					} finally {
						out.close();
					}
				} catch (IOException e) {
					try {
						lombokJar.delete();
					} catch (Throwable ignore) {}
					if (!readSucceeded) throw new InstallException("I can't read my own jar file. I think you've found a bug in this installer! I suggest you restart it " +
							"and use the 'what do I do' link, to manually install lombok. And tell us about this. Thanks!", e);
					throw new InstallException("I can't write to your Eclipse directory, probably because this installer does not have the access rights.", e);
				}
				
				/* legacy - delete lombok.eclipse.agent.jar if its there, which lombok no longer uses. */ {
					new File(lombokJar.getParentFile(), "lombok.eclipse.agent.jar").delete();
				}
				
				try {
					FileInputStream fis = new FileInputStream(iniFile);
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
					
					FileOutputStream fos = new FileOutputStream(iniFile);
					try {
						fos.write(newContents.toString().getBytes());
					} finally {
						fos.close();
					}
					installSucceeded = true;
				} catch (IOException e) {
					throw new InstallException("Cannot install lombok at " + path.getAbsolutePath() +
							" probably because this installer does not have the access rights to do so.", e);
				} finally {
					if (!installSucceeded) try {
						lombokJar.delete();
					} catch (Throwable ignore) {}
				}
			}
		}
		
		if (!installSucceeded) {
			throw new InstallException("I can't find the eclipse.ini file. Is this a real Eclipse installation?", null);
		}
		
		for (File dir : failedDirs) {
			/* Legacy code - lombok's installer used to install in other places. To keep the user's eclipse dir clean, we'll delete these. */ {
				try {
					new File(dir, "lombok.jar").delete();
					new File(dir, "lombok.eclipse.agent.jar").delete();
				} catch (Throwable ignore) {}
			}
		}
	}
}
