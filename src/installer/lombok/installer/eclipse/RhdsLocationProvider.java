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
public class RhdsLocationProvider extends EclipseLocationProvider {
	@Override protected List<String> getEclipseExecutableNames() {
		return Arrays.asList("devstudio.app", "devstudio.exe", "devstudioc.exe", "devstudio");
	}
	
	@Override protected String getIniName() {
		return "devstudio.ini";
	}
	
	@Override protected IdeLocation makeLocation(String name, File ini) throws CorruptedIdeLocationException {
		return new RhdsLocation(name, ini);
	}
	
	@Override protected String getMacAppName() {
		return "devstudio.app";
	}
	
	@Override protected String getUnixAppName() {
		return "devstudio";
	}
	
	@Override public Pattern getLocationSelectors(OS os) {
		switch (os) {
		case MAC_OS_X:
			return Pattern.compile("^(devstudio|devstudio\\.ini|devstudio\\.app)$", Pattern.CASE_INSENSITIVE);
		case WINDOWS:
			return Pattern.compile("^(devstudioc?\\.exe|devstudio\\.ini)$", Pattern.CASE_INSENSITIVE);
		default:
		case UNIX:
			return Pattern.compile("^(devstudio|devstudio\\.ini)$", Pattern.CASE_INSENSITIVE);
		}
	}
}
