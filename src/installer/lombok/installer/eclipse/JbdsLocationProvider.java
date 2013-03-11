/*
 * Copyright (C) 2013 The Project Lombok Authors.
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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import lombok.installer.CorruptedIdeLocationException;
import lombok.installer.IdeLocation;
import lombok.installer.IdeLocationProvider;
import lombok.installer.IdeFinder.OS;

import org.mangosdk.spi.ProviderFor;

@ProviderFor(IdeLocationProvider.class)
public class JbdsLocationProvider extends EclipseLocationProvider {
	@Override protected List<String> getEclipseExecutableNames() {
		return Arrays.asList("jbdevstudio.app", "jbdevstudio.exe", "jbdevstudioc.exe", "jbdevstudio");
	}
	
	@Override protected String getIniName() {
		return "jbdevstudio.ini";
	}
	
	@Override protected IdeLocation makeLocation(String name, File ini) throws CorruptedIdeLocationException {
		return new JbdsLocation(name, ini);
	}
	
	@Override protected String getMacAppName() {
		return "jbdevstudio.app";
	}
	
	@Override protected String getUnixAppName() {
		return "jbdevstudio";
	}
	
	@Override public Pattern getLocationSelectors(OS os) {
		switch (os) {
		case MAC_OS_X:
			return Pattern.compile("^(jbdevstudio|jbdevstudio\\.ini|jbdevstudio\\.app)$", Pattern.CASE_INSENSITIVE);
		case WINDOWS:
			return Pattern.compile("^(jbdevstudioc?\\.exe|jbdevstudio\\.ini)$", Pattern.CASE_INSENSITIVE);
		default:
		case UNIX:
			return Pattern.compile("^(jbdevstudio|jbdevstudio\\.ini)$", Pattern.CASE_INSENSITIVE);
		}
	}
}
