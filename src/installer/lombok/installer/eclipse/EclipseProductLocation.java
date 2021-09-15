/*
 * Copyright (C) 2009-2017 The Project Lombok Authors.
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
package lombok.installer.eclipse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.installer.CorruptedIdeLocationException;
import lombok.installer.OsUtils;
import lombok.installer.IdeLocation;
import lombok.installer.InstallException;
import lombok.installer.Installer;
import lombok.installer.UninstallException;

/**
 * Represents an Eclipse installation.
 * An instance can figure out if an Eclipse installation has been lombok-ified, and can
 * install and uninstall lombok from the Eclipse installation.
 */
public final class EclipseProductLocation extends IdeLocation {
	
	private static final String OS_NEWLINE = OsUtils.getOS().getLineEnding();
	
	private final EclipseProductDescriptor descriptor;
	private final String name;
	private final File eclipseIniPath;
	private final String pathToLombokJarPrefix;
	private final boolean hasLombok;
	
	EclipseProductLocation(EclipseProductDescriptor descriptor, String nameOfLocation, File pathToEclipseIni) throws CorruptedIdeLocationException {
		this.descriptor = descriptor;
		this.name = nameOfLocation;
		this.eclipseIniPath = pathToEclipseIni;
		File p1 = pathToEclipseIni.getParentFile();
		File p2 = p1 == null ? null : p1.getParentFile();
		File p3 = p2 == null ? null : p2.getParentFile();
		if (p1 != null && p1.getName().equals("Eclipse") && p2 != null && p2.getName().equals("Contents") && p3 != null && p3.getName().endsWith(".app")) {
			this.pathToLombokJarPrefix = "../Eclipse/";
		} else {
			this.pathToLombokJarPrefix = "";
		}
		
		try {
			this.hasLombok = checkForLombok(eclipseIniPath);
		} catch (IOException e) {
			throw new CorruptedIdeLocationException(
					"I can't read the configuration file of the " + descriptor.getProductName() + " installed at " + name + "\n" +
					"You may need to run this installer with root privileges if you want to modify that " + descriptor.getProductName() + ".", descriptor.getProductName(), e);
		}
	}
	
	@Override public int hashCode() {
		return eclipseIniPath.hashCode();
	}
	
	@Override public boolean equals(Object o) {
		if (!(o instanceof EclipseProductLocation)) return false;
		return ((EclipseProductLocation)o).eclipseIniPath.equals(eclipseIniPath);
	}
	
	/**
	 * Returns the name of this location; generally the path to the eclipse executable.
	 */
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * @return true if the Eclipse installation has been instrumented with lombok.
	 */
	@Override
	public boolean hasLombok() {
		return hasLombok;
	}
	
	private static final Pattern JAVA_AGENT_LINE_MATCHER = Pattern.compile(
			"^\\-javaagent\\:.*lombok.*\\.jar$", Pattern.CASE_INSENSITIVE);
	
	private static final Pattern BOOTCLASSPATH_LINE_MATCHER = Pattern.compile(
			"^\\-Xbootclasspath\\/a\\:(.*lombok.*\\.jar.*)$", Pattern.CASE_INSENSITIVE);
	
	private static boolean checkForLombok(File iniFile) throws IOException {
		if (!iniFile.exists()) return false;
		FileInputStream fis = new FileInputStream(iniFile);
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line;
			while ((line = br.readLine()) != null) {
				if (JAVA_AGENT_LINE_MATCHER.matcher(line.trim()).matches()) {
					br.close();
					return true;
				}
			}
			
			br.close();
			return false;
		} finally {
			fis.close();
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
	@Override 
	public void uninstall() throws UninstallException {
		final List<File> lombokJarsForWhichCantDeleteSelf = new ArrayList<File>();
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
					br.close();
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
		
		for (File dir : getUninstallDirs()) {
			File lombokJar = new File(dir, "lombok.jar");
			if (lombokJar.exists()) {
				if (!lombokJar.delete()) {
					if (OsUtils.getOS() == OsUtils.OS.WINDOWS && Installer.isSelf(lombokJar.getAbsolutePath())) {
						lombokJarsForWhichCantDeleteSelf.add(lombokJar);
					} else {
						throw new UninstallException(
							"Can't delete " + lombokJar.getAbsolutePath() + generateWriteErrorMessage(), null);
					}
				}
			}
			
			/* legacy code - lombok at one point used to have a separate jar for the eclipse agent.
			 * Leave this code in to delete it for those upgrading from an old version. */ {
				File agentJar = new File(dir, "lombok.eclipse.agent.jar");
				if (agentJar.exists()) {
					agentJar.delete();
				}
			}
		}
		
		if (!lombokJarsForWhichCantDeleteSelf.isEmpty()) {
			throw new UninstallException(true, String.format(
					"lombok.jar cannot delete itself on windows.\nHowever, lombok has been uncoupled from your %s.\n" +
					"You can safely delete this jar file. You can find it at:\n%s",
					descriptor.getProductName(), lombokJarsForWhichCantDeleteSelf.get(0).getAbsolutePath()), null);
		}
	}
	
	private static String generateWriteErrorMessage() {
		String osSpecificError;
		
		switch (OsUtils.getOS()) {
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
	@Override
	public String install() throws InstallException {
		// On Linux, for whatever reason, relative paths in your eclipse.ini file don't work, but only for -javaagent.
		// On Windows, since the Oomph, the generated shortcut starts in the wrong directory.
		// So the default is to use absolute paths, breaking lombok when you move the eclipse directory.
		// Or not break when you copy your directory, but break later when you remove the original one.
		boolean fullPathRequired = !"false".equals(System.getProperty("lombok.installer.fullpath", "true"));
		
		boolean installSucceeded = false;
		StringBuilder newContents = new StringBuilder();
		
		File lombokJar = new File(eclipseIniPath.getParentFile(), "lombok.jar");
		
		/* No need to copy lombok.jar to itself, obviously. On windows this would generate an error so we check for this. */
		if (!Installer.isSelf(lombokJar.getAbsolutePath())) {
			File ourJar = findOurJar();
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
				if (!readSucceeded) {
					throw new InstallException(
						"I can't read my own jar file (trying: " + ourJar.toString() + "). I think you've found a bug in this installer!\nI suggest you restart it " +
						"and use the 'what do I do' link, to manually install lombok. Also, tell us about this at:\n" +
						"http://groups.google.com/group/project-lombok - Thanks!\n\n[DEBUG INFO] " + e.getClass() + ": " + e.getMessage() + "\nBase: " + OsUtils.class.getResource("OsUtils.class"), e);
				}
				throw new InstallException("I can't write to your " + descriptor.getProductName() + " directory at " + name + generateWriteErrorMessage(), e);
			}
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
				br.close();
			} finally {
				fis.close();
			}
			
			String pathPrefix;
			if (fullPathRequired) {
				pathPrefix = lombokJar.getParentFile().getCanonicalPath() + File.separator;
			} else {
				pathPrefix = pathToLombokJarPrefix;
			}
			
			// NB: You may be tempted to escape this, but don't; there is no possibility to escape this, but
			// eclipse/java reads the string following the colon in 'raw' fashion. Spaces, colons - all works fine.
			newContents.append(String.format(
				"-javaagent:%s", pathPrefix + "lombok.jar")).append(OS_NEWLINE);
			
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
			throw new InstallException("I can't find the " + descriptor.getIniFileName() + " file. Is this a real " + descriptor.getProductName() + " installation?", null);
		}
		
		return "If you start " + descriptor.getProductName() + " with a custom -vm parameter, you'll need to add:<br>" +
				"<code>-vmargs -javaagent:lombok.jar</code><br>as parameter as well.";
	}
	
	@Override public URL getIdeIcon() {
		return descriptor.getIdeIcon();
	}
}
