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
package lombok.installer.netbeans;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lombok.installer.CorruptedIdeLocationException;
import lombok.installer.IdeFinder;
import lombok.installer.IdeLocation;

import org.mangosdk.spi.ProviderFor;

/** Utility class for doing various OS-specific operations related to finding Netbeans installations. */
@ProviderFor(IdeFinder.class)
public class NetbeansFinder extends IdeFinder {
	/**
	 * Returns a list of paths of Netbeans installations.
	 * 
	 * Netbeans installations are found by checking for the existence of 'netbeans.exe' in the following locations:
	 * <ul>
	 * <li>X:\*Program Files*\*NetBeans*</li>
	 * <li>X:\*NetBeans*</li>
	 * </ul>
	 * 
	 * Where 'X' is tried for all local disk drives, unless there's a problem calling fsutil, in which case only
	 * C: is tried.
	 */
	private void findNetbeansOnWindows(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems) {
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
						if (dir.getName().toLowerCase().contains("netbeans")) {
							String netbeansLocation = findNetbeansOnWindows1(dir);
							if (netbeansLocation != null) {
								try {
									locations.add(NetbeansLocationProvider.create0(netbeansLocation));
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
								if (dir2.getName().toLowerCase().contains("netbeans")) {
									String netbeansLocation = findNetbeansOnWindows1(dir2);
									if (netbeansLocation != null) {
										try {
											locations.add(NetbeansLocationProvider.create0(netbeansLocation));
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
	
	/** Checks if the provided directory contains 'netbeans.exe', and if so, returns the directory, otherwise null. */
	private String findNetbeansOnWindows1(File dir) {
		if (new File(dir, "netbeans.exe").isFile()) return dir.getAbsolutePath();
		return null;
	}
	
	/**
	 * Calls the OS-dependent 'find Netbeans' routine. If the local OS doesn't have a routine written for it,
	 * null is returned.
	 * 
	 * @param locations
	 *            List of valid netbeans locations - provide an empty list; this
	 *            method will fill it.
	 * @param problems
	 *            List of netbeans locations that seem to contain half-baked
	 *            netbeanses that can't be installed. Provide an empty list; this
	 *            method will fill it.
	 */
	@Override
	public void findIdes(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems) {
		switch (getOS()) {
		case WINDOWS:
			findNetbeansOnWindows(locations, problems);
			break;
		case MAC_OS_X:
			findNetbeansOnMac(locations, problems);
			break;
		default:
		case UNIX:
			findNetbeansOnUnix(locations, problems);
			break;
		}
	}
	
	/** Scans a couple of likely locations on linux. */
	private void findNetbeansOnUnix(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems) {
		List<String> guesses = new ArrayList<String>();
		
		File d;
		
		d = new File("/usr/bin/netbeans");
		if (d.exists()) guesses.add(d.getPath());
		d = new File("/usr/local/bin/netbeans");
		if (d.exists()) guesses.add(d.getPath());
		d = new File(System.getProperty("user.home", "."), "bin/netbeans");
		if (d.exists()) guesses.add(d.getPath());
		
		findNetbeansInSubDir("/usr/local/share", guesses);
		findNetbeansInSubDir("/usr/local", guesses);
		findNetbeansInSubDir("/usr/share", guesses);
		findNetbeansInSubDir(System.getProperty("user.home", "."), guesses);
		
		for (String guess : guesses) {
			try {
				locations.add(NetbeansLocationProvider.create0(guess));
			} catch (CorruptedIdeLocationException e) {
				problems.add(e);
			}
		}
	}
	
	private static void findNetbeansInSubDir(String dir, List<String> guesses) {
		File d = new File(dir);
		if (!d.isDirectory()) return;
		for (File f : d.listFiles()) {
			if (f.isDirectory() && f.getName().toLowerCase().contains("netbeans")) {
				File possible = new File(f, "bin/netbeans.exe");
				if (possible.exists()) guesses.add(possible.getAbsolutePath());
			}
		}
	}
	
	/**
	 * Scans /Applications for any folder named 'Eclipse'
	 */
	private static void findNetbeansOnMac(List<IdeLocation> locations, List<CorruptedIdeLocationException> problems) {
		for (File dir : new File("/Applications").listFiles()) {
			if (!dir.isDirectory()) continue;
			if (dir.getName().toLowerCase().startsWith("netbeans") && dir.getName().toLowerCase().endsWith(".app")) {
				try {
					locations.add(NetbeansLocationProvider.create0(dir.getAbsolutePath()));
				} catch (CorruptedIdeLocationException e) {
					problems.add(e);
				}
			}
			if (dir.getName().toLowerCase().contains("netbeans")) {
				for (File dir2 : dir.listFiles()) {
					if (!dir2.isDirectory()) continue;
					if (dir2.getName().toLowerCase().startsWith("netbeans") && dir2.getName().toLowerCase().endsWith(".app")) {
						try {
							locations.add(NetbeansLocationProvider.create0(dir2.getAbsolutePath()));
						} catch (CorruptedIdeLocationException e) {
							problems.add(e);
						}
					}
				}
			}
		}
	}
}
