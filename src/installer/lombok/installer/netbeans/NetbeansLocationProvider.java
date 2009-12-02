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

import static lombok.installer.IdeLocation.canonical;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import lombok.installer.CorruptedIdeLocationException;
import lombok.installer.IdeLocation;
import lombok.installer.IdeLocationProvider;
import lombok.installer.IdeFinder.OS;

import org.mangosdk.spi.ProviderFor;

@ProviderFor(IdeLocationProvider.class)
public class NetbeansLocationProvider implements IdeLocationProvider {
	
	@Override public IdeLocation create(String path) throws CorruptedIdeLocationException {
		return create0(path);
	}
	
	/**
	 * Create a new NetbeansLocation by pointing at either the directory containing the executable, or the executable itself,
	 * or a netbeans.conf file.
	 * 
	 * @throws NotAnIdeLocationException
	 *             If this isn't an Eclipse executable or a directory with an
	 *             Eclipse executable.
	 */
	static NetbeansLocation create0(String path) throws CorruptedIdeLocationException {
		if (path == null) throw new NullPointerException("path");
		File p = new File(path);
		
		if (!p.exists()) return null;
		if (p.isDirectory()) {
			String name = p.getName().toLowerCase();
			if (name.endsWith(".app") && name.startsWith("netbeans")) {
				File conf = new File(p, "Contents/Resources/NetBeans/etc/netbeans.conf");
				if (conf.exists()) return new NetbeansLocation(path, conf);
			}
			
			File f = new File(p, "bin/netbeans");
			if (f.isFile()) return findNetbeansConfFromExe(f, 0);
			f = new File(p, "bin/netbeans.exe");
			if (f.isFile()) return findNetbeansConfFromExe(f, 0);
			f = new File(p, "etc/netbeans.conf");
			if (f.isFile()) return new NetbeansLocation(canonical(f.getParentFile().getParentFile()), f);
		}
		
		if (p.isFile()) {
			if (p.getName().equalsIgnoreCase("netbeans.conf")) {
				return new NetbeansLocation(canonical(p.getParentFile().getParentFile()), p);
			}
			
			if (p.getName().equalsIgnoreCase("netbeans") || p.getName().equalsIgnoreCase("netbeans.exe")) {
				return findNetbeansConfFromExe(p, 0);
			}
		}
		
		return null;
	}
	
	private static NetbeansLocation findNetbeansConfFromExe(File exePath, int loopCounter) throws CorruptedIdeLocationException {
		/* Try looking for netbeans.conf as etc/netbeans.conf */ {
			File conf = new File(exePath.getParentFile(), "etc/netbeans.conf");
			if (conf.isFile()) return new NetbeansLocation(canonical(exePath), conf);
		}
		
		/* Try looking for netbeans.conf as ../etc/netbeans.conf */ {
			File conf = new File(exePath.getParentFile().getParentFile(), "etc/netbeans.conf");
			if (conf.isFile()) return new NetbeansLocation(canonical(exePath), conf);
		}
		
		/* If executable is a soft link, follow it and retry. */ {
			if (loopCounter < 50) {
				try {
					String oPath = exePath.getAbsolutePath();
					String nPath = exePath.getCanonicalPath();
					if (!oPath.equals(nPath)) try {
						return findNetbeansConfFromExe(new File(nPath), loopCounter + 1);
					} catch (CorruptedIdeLocationException ignore) {
						// Unlinking didn't help find a netbeans, so continue.
					}
				} catch (IOException ignore) { /* okay, that didn't work, assume it isn't a soft link then. */ }
			}
		}
		
		/* If we get this far, we lose. */
		return null;
	}
	
	@Override public Pattern getLocationSelectors(OS os) {
		switch (os) {
		case MAC_OS_X:
			return Pattern.compile("^(netbeans|netbeans\\.conf|NetBeans.*\\.app)$", Pattern.CASE_INSENSITIVE);
		case WINDOWS:
			return Pattern.compile("^(netbeans\\.exe|netbeans\\.conf)$", Pattern.CASE_INSENSITIVE);
		default:
		case UNIX:
			return Pattern.compile("^(netbeans|netbeans\\.conf)$", Pattern.CASE_INSENSITIVE);
		}
	}
}
