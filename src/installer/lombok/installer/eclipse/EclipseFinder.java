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
package lombok.installer.eclipse;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lombok.installer.IdeFinder;
import lombok.installer.IdeLocation;
import lombok.installer.CorruptedIdeLocationException;

import org.mangosdk.spi.ProviderFor;

@ProviderFor(IdeFinder.class)
public class EclipseFinder extends IdeFinder {
	/**
	 * Returns a list of paths of Eclipse installations.
	 * Eclipse installations are found by checking for the existence of 'eclipse.exe' in the following locations:
	 * <ul>
	 * <li>X:\*Program Files*\*Eclipse*</li>
	 * <li>X:\*Eclipse*</li>
	 * </ul>
	 * 
	 * Where 'X' is tried for all local disk drives, unless there's a problem calling fsutil, in which case only
	 * C: is tried.
	 */
	private static void findEclipseOnWindows(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems) {
		List<String> driveLetters = asList("C");
		try {
			driveLetters = getDrivesOnWindows();
		} catch (Throwable ignore) {
			ignore.printStackTrace();
		}
		
		//Various try/catch/ignore statements are in this for loop. Weird conditions on the disk can cause exceptions,
		//such as an unformatted drive causing a NullPointerException on listFiles. Best action is almost invariably to just
		//continue onwards.
		for (String letter : driveLetters) {
			try {
				File f = new File(letter + ":\\");
				for (File dir : f.listFiles()) {
					if (!dir.isDirectory()) continue;
					try {
						if (dir.getName().toLowerCase().contains("eclipse")) {
							String eclipseLocation = findEclipseOnWindows1(dir);
							if (eclipseLocation != null) {
								try {
									locations.add(EclipseLocationProvider.create0(eclipseLocation));
								} catch (CorruptedIdeLocationException e) {
									problems.add(e);
								}
							}
						}
					} catch (Exception ignore) {}
					
					try {
						if (dir.getName().toLowerCase().contains("program files")) {
							for (File dir2 : dir.listFiles()) {
								if (!dir2.isDirectory()) continue;
								if (dir2.getName().toLowerCase().contains("eclipse")) {
									String eclipseLocation = findEclipseOnWindows1(dir2);
									if (eclipseLocation != null) {
										try {
											locations.add(EclipseLocationProvider.create0(eclipseLocation));
										} catch (CorruptedIdeLocationException e) {
											problems.add(e);
										}
									}
								}
							}
						}
					} catch (Exception ignore) {}
				}
			} catch (Exception ignore) {}
		}
	}
	
	/** Checks if the provided directory contains 'eclipse.exe', and if so, returns the directory, otherwise null. */
	private static String findEclipseOnWindows1(File dir) {
		if (new File(dir, "eclipse.exe").isFile()) return dir.getAbsolutePath();
		return null;
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
			findEclipseOnWindows(locations, problems);
			break;
		case MAC_OS_X:
			findEclipseOnMac(locations, problems);
			break;
		default:
		case UNIX:
			findEclipseOnUnix(locations, problems);
			break;
		}
	}
	
	/** Scans a couple of likely locations on linux. */
	private void findEclipseOnUnix(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems) {
		List<String> guesses = new ArrayList<String>();
		
		File d;
		
		d = new File("/usr/bin/eclipse");
		if (d.exists()) guesses.add(d.getPath());
		d = new File("/usr/local/bin/eclipse");
		if (d.exists()) guesses.add(d.getPath());
		d = new File(System.getProperty("user.home", "."), "bin/eclipse");
		if (d.exists()) guesses.add(d.getPath());
		
		findEclipseInSubDir("/usr/local/share", guesses);
		findEclipseInSubDir("/usr/local", guesses);
		findEclipseInSubDir("/usr/share", guesses);
		findEclipseInSubDir(System.getProperty("user.home", "."), guesses);
		
		for (String guess : guesses) {
			try {
				locations.add(EclipseLocationProvider.create0(guess));
			} catch (CorruptedIdeLocationException e) {
				problems.add(e);
			}
		}
	}
	
	private void findEclipseInSubDir(String dir, List<String> guesses) {
		File d = new File(dir);
		if (!d.isDirectory()) return;
		for (File f : d.listFiles()) {
			if (f.isDirectory() && f.getName().toLowerCase().contains("eclipse")) {
				File possible = new File(f, "eclipse");
				if (possible.exists()) guesses.add(possible.getAbsolutePath());
			}
		}
	}
	
	/**
	 * Scans /Applications for any folder named 'Eclipse'
	 */
	private void findEclipseOnMac(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems) {
		for (File dir : new File("/Applications").listFiles()) {
			if (!dir.isDirectory()) continue;
			if (dir.getName().toLowerCase().equals("eclipse.app")) {
				//This would be kind of an unorthodox Eclipse installation, but if Eclipse ever
				//moves to this more maclike installation concept, our installer can still handle it.
				try {
					locations.add(EclipseLocationProvider.create0("/Applications"));
				} catch (CorruptedIdeLocationException e) {
					problems.add(e);
				}
			}
			if (dir.getName().toLowerCase().contains("eclipse")) {
				if (new File(dir, "Eclipse.app").exists()) {
					try {
						locations.add(EclipseLocationProvider.create0(dir.toString()));
					} catch (CorruptedIdeLocationException e) {
						problems.add(e);
					}
				}
			}
		}
	}
}
