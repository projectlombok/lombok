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
package lombok.installer.eclipse;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.installer.IdeFinder;
import lombok.installer.IdeLocation;
import lombok.installer.CorruptedIdeLocationException;

import org.mangosdk.spi.ProviderFor;

@ProviderFor(IdeFinder.class)
public class EclipseFinder extends IdeFinder {
	/** should be lowercase! */
	protected String getDirName() {
		return "eclipse";
	}
	
	protected String getWindowsExecutableName() {
		return "eclipse.exe";
	}
	
	protected String getUnixExecutableName() {
		return "eclipse";
	}
	
	protected String getMacExecutableName() {
		return "Eclipse.app";
	}
	
	protected IdeLocation createLocation(String guess) throws CorruptedIdeLocationException {
		return new EclipseLocationProvider().create0(guess);
	}
	
	protected List<String> getSourceDirsOnWindows() {
		return Arrays.asList("\\", "\\Program Files", "\\Program Files (x86)", System.getProperty("user.home", "."));
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
			driveLetters = getDrivesOnWindows();
		} catch (Throwable ignore) {
			ignore.printStackTrace();
		}
		List<String> sourceDirs = new ArrayList<String>();
		for (String letter : driveLetters) {
			for (String possibleSource : getSourceDirsOnWindows()) {
				if (!isDriveSpecificOnWindows(possibleSource)) {
					sourceDirs.add(letter + ":" + possibleSource);
				}
			}
		}
		for (String possibleSource : getSourceDirsOnWindows()) {
			if (isDriveSpecificOnWindows(possibleSource)) sourceDirs.add(possibleSource);
		}
		
		return sourceDirs;
	}
	
	public boolean isDriveSpecificOnWindows(String path) {
		return path.length() > 1 && path.charAt(1) == ':';
	}
	
	protected List<String> getSourceDirsOnMac() {
		return Arrays.asList("/Applications", System.getProperty("user.home", "."));
	}
	
	protected List<String> getSourceDirsOnUnix() {
		return Arrays.asList(System.getProperty("user.home", "."));
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
		switch (getOS()) {
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
	
	private class UnixFinder extends DirectoryFinder {
		UnixFinder() {
			super(getNestedSourceLocationOnUnix(), getFlatSourceLocationsOnUnix());
		}
		
		@Override protected String findEclipseOnPlatform(File dir) {
			File possible = new File(dir, getUnixExecutableName());
			return (possible.exists()) ? possible.getAbsolutePath() : null;
		}
	}
	
	private class WindowsFinder extends DirectoryFinder {
		WindowsFinder() {
			super(transformToFiles(getSourceDirsOnWindowsWithDriveLetters()), Collections.<File>emptyList());
		}
		
		/** Checks if the provided directory contains 'eclipse.exe', and if so, returns the directory, otherwise null. */
		@Override 
		protected String findEclipseOnPlatform(File dir) {
			File possible = new File(dir, getWindowsExecutableName());
			return (possible.isFile()) ? dir.getAbsolutePath() : null;
		}
	}
	
	private class MacFinder extends DirectoryFinder {
		MacFinder() {
			super(transformToFiles(getSourceDirsOnMac()), Collections.<File>emptyList());
		}
		
		protected String findEclipseOnPlatform(File dir) {
			if (dir.getName().toLowerCase().equals(getMacExecutableName().toLowerCase())) return dir.getParent();
			if (dir.getName().toLowerCase().contains(getDirName())) {
				if (new File(dir, getMacExecutableName()).exists()) return dir.toString();
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
		
		public void findEclipse(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems) {
			for (File dir : nestedSourceDirs) recurseDirectory(locations, problems, dir);
			for (File dir : flatSourceDirs) findEclipse(locations, problems, dir);
		}
		
		protected abstract String findEclipseOnPlatform(File dir);
		
		protected void recurseDirectory(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems, File dir) {
			recurseDirectory0(locations, problems, dir, 0);
		}
		
		private void recurseDirectory0(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems, File f, int loopCounter) {
			//Various try/catch/ignore statements are in this for loop. Weird conditions on the disk can cause exceptions,
			//such as an unformatted drive causing a NullPointerException on listFiles. Best action is almost invariably to just
			//continue onwards.
			File[] listFiles = f.listFiles();
			if (listFiles == null) return;
			
			for (File dir : listFiles) {
				if (!dir.isDirectory()) continue;
				try {
					if (dir.getName().toLowerCase().contains(getDirName())) {
						findEclipse(locations, problems, dir);
						if (loopCounter < 50) recurseDirectory0(locations, problems, dir, loopCounter + 1);
					}
				} catch (Exception ignore) {}
			}
		}

		private void findEclipse(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems, File dir) {
			String eclipseLocation = findEclipseOnPlatform(dir);
			if (eclipseLocation != null) {
				try {
					IdeLocation newLocation = createLocation(eclipseLocation);
					if (newLocation != null) locations.add(newLocation);
				} catch (CorruptedIdeLocationException e) {
					problems.add(e);
				}
			}
		}
	}
}
