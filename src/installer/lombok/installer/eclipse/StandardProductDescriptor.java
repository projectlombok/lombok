/*
 * Copyright (C) 2016 The Project Lombok Authors.
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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import lombok.installer.OsUtils;

public class StandardProductDescriptor implements EclipseProductDescriptor {
	
	private static final String USER_HOME = System.getProperty("user.home", ".");
	private static final String[] WINDOWS_ROOTS = windowsRoots();
	private static final String[] MAC_ROOTS = {"/Applications", USER_HOME};
	private static final String[] UNIX_ROOTS = {USER_HOME};
	
	private final String productName;
	private final String windowsName;
	private final String unixName;
	private final String macAppName;
	private final List<String> executableNames;
	private final List<String> sourceDirsOnWindows;
	private final List<String> sourceDirsOnMac;
	private final List<String> sourceDirsOnUnix;
	private final String iniFileName;
	private final Pattern locationSelectors;
	private final String directoryName;
	private final URL ideIcon;
	
	public StandardProductDescriptor(String productName, String baseName, String directoryName, URL ideIcon, Collection<String> alternativeDirectoryNames) {
		this.productName = productName;
		this.windowsName = baseName + ".exe";
		this.unixName = baseName;
		this.macAppName = baseName + ".app";
		this.executableNames = executableNames(baseName);
		this.sourceDirsOnWindows = generateAlternatives(WINDOWS_ROOTS, "\\", alternativeDirectoryNames);
		this.sourceDirsOnMac = generateAlternatives(MAC_ROOTS, "/", alternativeDirectoryNames);
		this.sourceDirsOnUnix = generateAlternatives(UNIX_ROOTS, "/", alternativeDirectoryNames);
		this.iniFileName = baseName + ".ini";
		this.locationSelectors = getLocationSelectors(baseName);
		this.directoryName = directoryName.toLowerCase();
		this.ideIcon = ideIcon;
	}
	
	@Override public String getProductName() {
		return productName;
	}
	
	@Override public String getWindowsExecutableName() {
		return windowsName;
	}
	
	@Override public String getUnixAppName() {
		return unixName;
	}
	
	@Override public String getMacAppName() {
		return macAppName;
	}
	
	@Override public String getDirectoryName() {
		return directoryName;
	}
	
	@Override public List<String> getExecutableNames() {
		return executableNames;
	}
	
	@Override public List<String> getSourceDirsOnWindows() {
		return sourceDirsOnWindows;
	}
	
	@Override public List<String> getSourceDirsOnMac() {
		return sourceDirsOnMac;
	}
	
	@Override public List<String> getSourceDirsOnUnix() {
		return sourceDirsOnUnix;
	}
	
	@Override public String getIniFileName() {
		return iniFileName;
	}
	
	@Override public Pattern getLocationSelectors() {
		return locationSelectors;
	}
	
	@Override public URL getIdeIcon() {
		return ideIcon;
	}
	
	private static Pattern getLocationSelectors(String baseName) {
		return Pattern.compile(String.format(platformPattern(), baseName.toLowerCase()), Pattern.CASE_INSENSITIVE);
	}
	
	private static String platformPattern() {
		switch (OsUtils.getOS()) {
		case MAC_OS_X:
			return "^(%s|%<s\\.ini|%<s\\.app)$";
		case WINDOWS:
			return "^(%sc?\\.exe|%<s\\.ini)$";
		default:
		case UNIX:
			return "^(%s|%<s\\.ini)$";
		}
	}
	
	private static List<String> executableNames(String baseName) {
		String base = baseName.toLowerCase();
		return Collections.unmodifiableList(Arrays.asList(base, base + ".app", base + ".exe", base + "c.exe"));
	}
	
	private static List<String> generateAlternatives(String[] roots, String pathSeparator, Collection<String> alternatives) {
		List<String> result = new ArrayList<String>();
		for (String root : roots) {
			result.add(concat(root, pathSeparator, ""));
			for (String alternative : alternatives) {
				result.add(concat(root, pathSeparator, alternative));
			}
		}
		return Collections.unmodifiableList(result);
	}
	
	private static String concat(String base, String pathSeparator, String alternative) {
		if (alternative.isEmpty()) {
			return base;
		}
		if (base.endsWith(pathSeparator)) {
			return base + alternative.replaceAll("[\\/]", "\\" + pathSeparator);
		}
		return base + pathSeparator + alternative.replaceAll("[\\/]", "\\" + pathSeparator);
	}
	
	private static String[] windowsRoots() {
		String localAppData = windowsLocalAppData();
		if (localAppData == null) return new String[] {"\\", "\\Program Files", "\\Program Files (x86)", USER_HOME};
		return new String[] {"\\", "\\Program Files", "\\Program Files (x86)", USER_HOME, localAppData};
	}
	
	private static String windowsLocalAppData() {
		String localAppData = System.getenv("LOCALAPPDATA");
		File file = localAppData == null ? null : new File(localAppData);
		return file != null && file.exists() && file.canRead() && file.isDirectory() ? localAppData : null;
	}
}
