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
package lombok.installer.eclipse;

import static java.util.Arrays.asList;
import static lombok.installer.IdeLocation.canonical;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import lombok.installer.CorruptedIdeLocationException;
import lombok.installer.OsUtils;
import lombok.installer.IdeLocation;
import lombok.installer.IdeLocationProvider;

public class EclipseProductLocationProvider implements IdeLocationProvider {
	private final EclipseProductDescriptor descriptor;

	EclipseProductLocationProvider(EclipseProductDescriptor descriptor) {
		this.descriptor = descriptor;
	}
	
	/**
	 * Create a new EclipseLocation by pointing at either the directory contains the Eclipse executable, or the executable itself,
	 * or an eclipse.ini file.
	 * 
	 * @throws CorruptedIdeLocationException
	 *             If this isn't an Eclipse executable or a directory with an
	 *             Eclipse executable.
	 * @throws NullPointerException if {@code path} is {@code null}.
	 */
	@Override public final IdeLocation create(String path) throws CorruptedIdeLocationException {
		return create0(path);
	}
	
	private IdeLocation create0(String path) throws CorruptedIdeLocationException {
		if (path == null) throw new NullPointerException("path");
		String iniName = descriptor.getIniFileName();
		File p = new File(path);
		
		if (!p.exists()) return null;
		if (p.isDirectory()) {
			for (String possibleExeName : descriptor.getExecutableNames()) {
				File f = new File(p, possibleExeName);
				if (f.exists()) return findEclipseIniFromExe(f, 0);
			}
			
			File f = new File(p, iniName);
			if (f.exists()) return makeLocation(new String[] {canonical(p)}, new File[] {f});
		}
		
		if (p.isFile()) {
			if (p.getName().equalsIgnoreCase(iniName)) {
				return makeLocation(new String[] {canonical(p.getParentFile())}, new File[] {p});
			}
		}
		
		if (descriptor.getExecutableNames().contains(p.getName().toLowerCase())) {
			return findEclipseIniFromExe(p, 0);
		}
		
		return null;
	}
	
	private IdeLocation findEclipseIniFromExe(File exePath, int loopCounter) throws CorruptedIdeLocationException {
		String iniName = descriptor.getIniFileName();
		List<Object> foundResults = new ArrayList<Object>(); // even indices are the canonical path (Strings), the immediately following odd index is the file. (java.io.File).
		/* Try looking for eclipse.ini as sibling to the executable */ {
			File ini = new File(exePath.getParentFile(), iniName);
			if (ini.isFile()) {
				foundResults.add(canonical(exePath));
				foundResults.add(ini);
			}
		}
		
		String macAppName = descriptor.getMacAppName();
		
		/* Starting with Eclipse Mars (with the oomph installer), the structure has changed, and it's now at Eclipse.app/Contents/Eclipse/eclipse.ini*/ {
			File ini = new File(exePath.getParentFile(), macAppName + "/Contents/Eclipse/" + iniName);
			if (ini.isFile()) {
				foundResults.add(canonical(exePath));
				foundResults.add(ini);
			}
		}
		
		/* Try looking for Eclipse.app/Contents/MacOS/eclipse.ini as sibling to executable; this works on Mac OS X. */ {
			File ini = new File(exePath.getParentFile(), macAppName + "/Contents/MacOS/" + iniName);
			if (ini.isFile()) {
				foundResults.add(canonical(exePath));
				foundResults.add(ini);
			}
		}
		
		if (foundResults.size() > 0) {
			String[] paths = new String[foundResults.size() / 2];
			File[] files = new File[paths.length];
			
			for (int i = 0; i < paths.length; i++) {
				paths[i] = (String) foundResults.get(i * 2);
				files[i] = (File) foundResults.get((i * 2) + 1);
			}
			
			return makeLocation(paths, files);
		}
		
		/* If executable is a soft link, follow it and retry. */ {
			if (loopCounter < 50) {
				try {
					String oPath = exePath.getAbsolutePath();
					String nPath = exePath.getCanonicalPath();
					if (!oPath.equals(nPath)) try {
						IdeLocation loc = findEclipseIniFromExe(new File(nPath), loopCounter + 1);
						if (loc != null) return loc;
					} catch (CorruptedIdeLocationException ignore) {
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
			
			String unixAppName = descriptor.getUnixAppName();
			if (path.equals("/usr/bin/" + unixAppName) || path.equals("/bin/" + unixAppName) || path.equals("/usr/local/bin/" + unixAppName)) {
				File ini = new File("/usr/lib/" + unixAppName + "/" + iniName);
				if (ini.isFile()) return makeLocation(new String[] {path}, new File[] {ini});
				ini = new File("/usr/local/lib/" + unixAppName + "/" + iniName);
				if (ini.isFile()) return makeLocation(new String[] {path}, new File[] {ini});
				ini = new File("/usr/local/etc/" + unixAppName + "/" + iniName);
				if (ini.isFile()) return makeLocation(new String[] {path}, new File[] {ini});
				ini = new File("/etc/" + iniName);
				if (ini.isFile()) return makeLocation(new String[] {path}, new File[] {ini});
			}
		}
		
		/* If we get this far, we lose. */
		return null;
	}
	
	private IdeLocation makeLocation(String[] name, File[] ini) throws CorruptedIdeLocationException {
		return new EclipseProductLocation(descriptor, name, ini);
	}
	
	@Override public Pattern getLocationSelectors() {
		return descriptor.getLocationSelectors();
	}
	
	/**
	 * Calls the OS-dependent 'find Eclipse' routine. If the local OS doesn't have a routine written for it,
	 * null is returned.
	 * 
	 * @param locations
	 *            List of valid eclipse locations - provide an empty list; this
	 *            method will fill it.
	 * @param problems
	 *            List of eclipse locations that seem to contain half-baked
	 *            eclipses that can't be installed. Provide an empty list; this
	 *            method will fill it.
	 */
	@Override
	public void findIdes(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems) {
		switch (OsUtils.getOS()) {
		case WINDOWS:
			new WindowsFinder().findEclipse(locations, problems);
			break;
		case MAC_OS_X:
			new MacFinder().findEclipse(locations, problems);
			break;
		default:
		case UNIX:
			new UnixFinder().findEclipse(locations, problems);
			break;
		}
	}
	
	private List<File> transformToFiles(List<String> fileNames) {
		List<File> files = new ArrayList<File>();
		for (String fileName : fileNames) {
			files.add(new File(fileName));
		}
		return files;
	}
	
	private List<File> getFlatSourceLocationsOnUnix() {
		List<File> dirs = new ArrayList<File>();
		dirs.add(new File("/usr/bin/"));
		dirs.add(new File("/usr/local/bin/"));
		dirs.add(new File(System.getProperty("user.home", "."), "bin/"));
		return dirs;
	}
	
	private List<File> getNestedSourceLocationOnUnix() {
		List<File> dirs = new ArrayList<File>();
		dirs.add(new File("/usr/local/share"));
		dirs.add(new File("/usr/local"));
		dirs.add(new File("/usr/share"));
		return dirs;
	}
	
	private class UnixFinder extends DirectoryFinder {
		UnixFinder() {
			super(getNestedSourceLocationOnUnix(), getFlatSourceLocationsOnUnix());
		}
		
		@Override protected String findEclipseOnPlatform(File dir) {
			File possible = new File(dir, descriptor.getUnixAppName());
			return (possible.exists()) ? possible.getAbsolutePath() : null;
		}
	}
	
	/**
	 * Returns a list of paths of Eclipse installations.
	 * 
	 * The search process works by scanning for each 'source dir' for either an eclipse installation or a folder containing the text returned
	 * by getDirName(). If such a folder is found, this process is applied recursively. On windows, this process is run on each drive letter
	 * which represents a physical hard disk. If the native windows API call to determine these drive letters fails, only 'C:' is checked.
	 */
	private List<String> getSourceDirsOnWindowsWithDriveLetters() {
		List<String> driveLetters = asList("C");
		try {
			driveLetters = OsUtils.getDrivesOnWindows();
		} catch (Throwable ignore) {
			ignore.printStackTrace();
		}
		List<String> sourceDirs = new ArrayList<String>();
		for (String letter : driveLetters) {
			for (String possibleSource : descriptor.getSourceDirsOnWindows()) {
				if (!isDriveSpecificOnWindows(possibleSource)) {
					sourceDirs.add(letter + ":" + possibleSource);
				}
			}
		}
		for (String possibleSource : descriptor.getSourceDirsOnWindows()) {
			if (isDriveSpecificOnWindows(possibleSource)) sourceDirs.add(possibleSource);
		}
		
		return sourceDirs;
	}
	
	private boolean isDriveSpecificOnWindows(String path) {
		return path.length() > 1 && path.charAt(1) == ':';
	}
	
	private class WindowsFinder extends DirectoryFinder {
		WindowsFinder() {
			super(transformToFiles(getSourceDirsOnWindowsWithDriveLetters()), Collections.<File>emptyList());
		}
		
		/** Checks if the provided directory contains 'eclipse.exe', and if so, returns the directory, otherwise null. */
		@Override 
		protected String findEclipseOnPlatform(File dir) {
			File possible = new File(dir, descriptor.getWindowsExecutableName());
			return (possible.isFile()) ? dir.getAbsolutePath() : null;
		}
	}
	
	private class MacFinder extends DirectoryFinder {
		MacFinder() {
			super(transformToFiles(descriptor.getSourceDirsOnMac()), Collections.<File>emptyList());
		}
		
		protected String findEclipseOnPlatform(File dir) {
			if (dir.getName().toLowerCase().equals(descriptor.getMacAppName().toLowerCase())) return dir.getParent();
			if (dir.getName().toLowerCase().contains(descriptor.getDirectoryName())) {
				if (new File(dir, descriptor.getMacAppName()).exists()) return dir.toString();
			}
			return null;
		}
	}
	
	private abstract class DirectoryFinder {
		private final List<File> flatSourceDirs;
		private final List<File> nestedSourceDirs;

		DirectoryFinder(List<File> nestedSourceDirs, List<File> flatSourceDirs) {
			this.nestedSourceDirs = nestedSourceDirs;
			this.flatSourceDirs = flatSourceDirs;
		}
		
		void findEclipse(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems) {
			for (File dir : nestedSourceDirs) recurseDirectory(locations, problems, dir);
			for (File dir : flatSourceDirs) findEclipse(locations, problems, dir);
		}
		
		abstract String findEclipseOnPlatform(File dir);
		
		void recurseDirectory(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems, File dir) {
			recurseDirectory0(locations, problems, dir, 0, false);
		}
		
		private void recurseDirectory0(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems, File f, int loopCounter, boolean nameFound) {
			//Various try/catch/ignore statements are in this for loop. Weird conditions on the disk can cause exceptions,
			//such as an unformatted drive causing a NullPointerException on listFiles. Best action is almost invariably to just
			//continue onwards.
			File[] listFiles = f.listFiles();
			if (listFiles == null) return;
			
			for (File dir : listFiles) {
				if (!dir.isDirectory()) continue;
				try {
					if (nameFound || dir.getName().toLowerCase().contains(descriptor.getDirectoryName())) {
						findEclipse(locations, problems, dir);
						if (loopCounter < 50) recurseDirectory0(locations, problems, dir, loopCounter + 1, true);
					}
				} catch (Exception ignore) {}
			}
		}
		
		private void findEclipse(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems, File dir) {
			String eclipseLocation = findEclipseOnPlatform(dir);
			if (eclipseLocation != null) {
				try {
					IdeLocation newLocation = create(eclipseLocation);
					if (newLocation != null) locations.add(newLocation);
				} catch (CorruptedIdeLocationException e) {
					problems.add(e);
				}
			}
		}
	}
}
