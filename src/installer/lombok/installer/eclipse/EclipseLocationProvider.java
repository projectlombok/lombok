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

import static lombok.installer.IdeLocation.canonical;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import lombok.installer.IdeLocation;
import lombok.installer.IdeLocationProvider;
import lombok.installer.CorruptedIdeLocationException;
import lombok.installer.IdeFinder.OS;

import org.mangosdk.spi.ProviderFor;

@ProviderFor(IdeLocationProvider.class)
public class EclipseLocationProvider implements IdeLocationProvider {
	@Override public IdeLocation create(String path) throws CorruptedIdeLocationException {
		return create0(path);
	}
	
	protected List<String> getEclipseExecutableNames() {
		return Arrays.asList("eclipse.app", "eclipse.exe", "eclipse");
	}
	
	protected String getIniName() {
		return "eclipse.ini";
	}
	
	protected IdeLocation makeLocation(String name, File ini) throws CorruptedIdeLocationException {
		return new EclipseLocation(name, ini);
	}
	
	protected String getMacAppName() {
		return "Eclipse.app";
	}
	
	protected String getUnixAppName() {
		return "eclipse";
	}
	
	/**
	 * Create a new EclipseLocation by pointing at either the directory contains the Eclipse executable, or the executable itself,
	 * or an eclipse.ini file.
	 * 
	 * @throws NotAnIdeLocationException
	 *             If this isn't an Eclipse executable or a directory with an
	 *             Eclipse executable.
	 */
	protected IdeLocation create0(String path) throws CorruptedIdeLocationException {
		if (path == null) throw new NullPointerException("path");
		File p = new File(path);
		
		if (!p.exists()) return null;
		if (p.isDirectory()) {
			for (String possibleExeName : getEclipseExecutableNames()) {
				File f = new File(p, possibleExeName);
				if (f.exists()) return findEclipseIniFromExe(f, 0);
			}
			
			File f = new File(p, getIniName());
			if (f.exists()) return new EclipseLocation(canonical(p), f);
		}
		
		if (p.isFile()) {
			if (p.getName().equalsIgnoreCase(getIniName())) {
				return new EclipseLocation(canonical(p.getParentFile()), p);
			}
		}
		
		if (getEclipseExecutableNames().contains(p.getName().toLowerCase())) {
			return findEclipseIniFromExe(p, 0);
		}
		
		return null;
	}
	
	private IdeLocation findEclipseIniFromExe(File exePath, int loopCounter) throws CorruptedIdeLocationException {
		/* Try looking for eclipse.ini as sibling to the executable */ {
			File ini = new File(exePath.getParentFile(), getIniName());
			if (ini.isFile()) return makeLocation(canonical(exePath), ini);
		}
		
		/* Try looking for Eclipse.app/Contents/MacOS/eclipse.ini as sibling to executable; this works on Mac OS X. */ {
			File ini = new File(exePath.getParentFile(), getMacAppName() + "/Contents/MacOS/" + getIniName());
			if (ini.isFile()) return makeLocation(canonical(exePath), ini);
		}
		
		/* Starting with Eclipse Mars (with the oomph installer), the structure has changed, and it's now at Eclipse.app/Contents/Eclipse/eclipse.ini*/ {
			File ini = new File(exePath.getParentFile(), getMacAppName() + "/Contents/Eclipse/" + getIniName());
			if (ini.isFile()) return makeLocation(canonical(exePath), ini);
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
			
			if (path.equals("/usr/bin/" + getUnixAppName()) || path.equals("/bin/" + getUnixAppName()) || path.equals("/usr/local/bin/" + getUnixAppName())) {
				File ini = new File("/usr/lib/" + getUnixAppName() + "/" + getIniName());
				if (ini.isFile()) return makeLocation(path, ini);
				ini = new File("/usr/local/lib/" + getUnixAppName() + "/" + getIniName());
				if (ini.isFile()) return makeLocation(path, ini);
				ini = new File("/usr/local/etc/" + getUnixAppName() + "/" + getIniName());
				if (ini.isFile()) return makeLocation(path, ini);
				ini = new File("/etc/" + getIniName());
				if (ini.isFile()) return makeLocation(path, ini);
			}
		}
		
		/* If we get this far, we lose. */
		return null;
	}
	
	@Override public Pattern getLocationSelectors(OS os) {
		switch (os) {
		case MAC_OS_X:
			return Pattern.compile("^(eclipse|eclipse\\.ini|eclipse\\.app)$", Pattern.CASE_INSENSITIVE);
		case WINDOWS:
			return Pattern.compile("^(eclipse\\.exe|eclipse\\.ini)$", Pattern.CASE_INSENSITIVE);
		default:
		case UNIX:
			return Pattern.compile("^(eclipse|eclipse\\.ini)$", Pattern.CASE_INSENSITIVE);
		}
	}
}
