/*
 * Copyright (C) 2009 The Project Lombok Authors.
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

import java.util.ArrayList;
import java.util.List;

/**
 * This class uses native calls on windows to figure out all drives,
 * and, for each drive, if its a harddisk or something else.
 * 
 * The output is essentially equivalent to running windows executable:
 * <pre>fsutil fsinfo drives</pre>
 * and
 * <pre>fsutil fsinfo drivetype C:</pre>
 * 
 * except that (A) fsutil requires privileges, (B) someone might have moved
 * it out of the path or some such, and (C) its output is internationalized,
 * so unless you want to include a table of how to say "Fixed Disk" in 300
 * languages, this really is a superior solution.
 * <p>
 * To compile it, you'll need windows, as well as MinGW:
 * http://sourceforge.net/projects/mingw/files/
 * <p>
 * Fetch gcc 4.0.4+, you don't need anything extra. Toss /c/mingw/bin in
 * your git bash prompt's path (/etc/profile) and then run:
 * 
 * $ gcc -c \
      -I "/c/Program Files/Java/jdk1.6.0_14/include" \
     -I "/c/Program Files/Java/jdk1.6.0_14/include/win32" \
     -D__int64="long long" lombok_installer_WindowsDriveInfo.c
 * 
 * $ dllwrap.exe --add-stdcall-alias \
      -o WindowsDriveInfo-i386.dll \
      lombok_installer_WindowsDriveInfo.o
 * 
 * You may get a warning along the lines of "Creating an export definition".
 * This is expected behaviour.
 * 
 * <p>
 * Now download MinGW-w64 to build the 64-bit version of the dll (you thought you were done, weren't you?)
 * from: http://sourceforge.net/projects/mingw-w64/files/
 *  (under toolchains targetting Win64 / Release for GCC 4.4.0 (or later) / the version for your OS.)
 * 
 * Then, do this all over again, but this time with the  x86_64-w64-mingw32-gcc and
 *  x86_64-w64-mingw32-dllwrap versions that are part of the MinGW-w64 distribution.
 *  Name the dll 'WindowsDriveInfo-x86_64.dll'.
 * 
 * Both the 32-bit and 64-bit DLLs that this produces have been checked into the git repository
 * under src/lombok/installer so you won't need to build them again unless you make some changes to
 * the code in the winsrc directory.
 */
public class WindowsDriveInfo {
	/**
	 * Return a list of all available drive letters, such as ["A", "C", "D"].
	 */
	public List<String> getLogicalDrives() {
		int flags = getLogicalDrives0();
		
		List<String> letters = new ArrayList<String>();
		for (int i = 0; i < 26; i++) {
			if ((flags & (1 << i)) != 0) letters.add(Character.toString((char)('A' + i)));
		}
		
		return letters;
	}
	
	/**
	 * Calls kernel32's GetLogicalDrives, which returns an int containing
	 * flags; bit 0 corresponds to drive A, bit 25 to drive Z. on = disk exists.
	 */
	private native int getLogicalDrives0();
	
	/**
	 * Feed it a drive letter (such as 'A') to see if it is a fixed disk.
	 */
	public boolean isFixedDisk(String letter) {
		if (letter.length() != 1) throw new IllegalArgumentException("Supply 1 letter, not: " + letter);
		char drive = Character.toUpperCase(letter.charAt(0));
		if (drive < 'A' || drive > 'Z') throw new IllegalArgumentException(
				"A drive is indicated by a letter, so A-Z inclusive. Not " + drive);
		return getDriveType(drive + ":\\") == 3L;
	}
	
	/**
	 * Mirror of kernel32's GetDriveTypeA. You must pass in 'A:\\' - 
	 * so including both a colon and a backslash!
	 * 
	 * 0 = error
	 * 1 = doesn't exist
	 * 2 = removable drive
	 * 3 = fixed disk
	 * 4 = remote (network) disk
	 * 5 = cd-rom
	 * 6 = ram disk
	 */
	private native int getDriveType(String name);
	
	public static void main(String[] args) {
		System.loadLibrary("WindowsDriveInfo");
		WindowsDriveInfo info = new WindowsDriveInfo();
		
		for (String letter : info.getLogicalDrives()) {
			System.out.printf("Drive %s: - %s\n", letter,
					info.isFixedDisk(letter) ? "Fixed Disk" : "Not Fixed Disk");
		}
	}
}
