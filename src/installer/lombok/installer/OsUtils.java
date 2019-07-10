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
package lombok.installer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.core.Version;

/**
 * Implement and provide this class to add auto-finding a certain brand of IDEs to the lombok installer.
 */
public final class OsUtils {
	private static final AtomicBoolean windowsDriveInfoLibLoaded = new AtomicBoolean(false);
	
	private OsUtils() {
		// Prevent instantiation
	}
	
	private static void loadWindowsDriveInfoLib() throws IOException {
		if (!windowsDriveInfoLibLoaded.compareAndSet(false, true)) return;
		
		final String prefix = "lombok-" + Version.getVersion() + "-";
		
		File temp = File.createTempFile("lombok", ".mark");
		File dll1 = new File(temp.getParentFile(), prefix + "WindowsDriveInfo-i386.dll");
		File dll2 = new File(temp.getParentFile(), prefix + "WindowsDriveInfo-x86_64.dll");
		temp.delete();
		dll1.deleteOnExit();
		dll2.deleteOnExit();
		try {
			if (unpackDLL("WindowsDriveInfo-i386.binary", dll1)) {
				System.load(dll1.getAbsolutePath());
				return;
			}
		} catch (Throwable ignore) {}
		
		try {
			if (unpackDLL("WindowsDriveInfo-x86_64.binary", dll2)) {
				System.load(dll2.getAbsolutePath());
			}
		} catch (Throwable ignore) {}
	}
	
	private static boolean unpackDLL(String dllName, File target) throws IOException {
		InputStream in = OsUtils.class.getResourceAsStream(dllName);
		try {
			try {
				FileOutputStream out = new FileOutputStream(target);
				try {
					byte[] b = new byte[32000];
					while (true) {
						int r = in.read(b);
						if (r == -1) break;
						out.write(b, 0, r);
					}
				} finally {
					out.close();
				}
			} catch (IOException e) {
				//Fall through - if there is a file named lombok-WindowsDriveInfo-arch.dll, we'll try it.
				return target.exists() && target.canRead();
			}
		} finally {
			in.close();
		}
		
		return true;
	}
	
	/**
	 * Returns all drive letters on windows that represent fixed disks.
	 * 
	 * Floppy drives, optical drives, USB sticks, and network drives should all be excluded.
	 * 
	 * @return A List of drive letters, such as ["C", "D", "X"].
	 */
	public static List<String> getDrivesOnWindows() throws Throwable {
		loadWindowsDriveInfoLib();
		
		List<String> drives = new ArrayList<String>();
		
		WindowsDriveInfo info = new WindowsDriveInfo();
		for (String drive : info.getLogicalDrives()) {
			if (info.isFixedDisk(drive)) drives.add(drive);
		}
		
		return drives;
	}
	
	public static enum OS {
		MAC_OS_X("\n"), WINDOWS("\r\n"), UNIX("\n");
		
		private final String lineEnding;
		
		OS(String lineEnding) {
			this.lineEnding = lineEnding;
		}
		
		public String getLineEnding() {
			return lineEnding;
		}
	}
	
	public static OS getOS() {
		String prop = System.getProperty("os.name", "").toLowerCase();
		if (prop.matches("^.*\\bmac\\b.*$")) return OS.MAC_OS_X;
		if (prop.matches("^.*\\bdarwin\\b.*$")) return OS.MAC_OS_X;
		if (prop.matches("^.*\\bwin(dows|32|64)?\\b.*$")) return OS.WINDOWS;
		
		return OS.UNIX;
	}
}
