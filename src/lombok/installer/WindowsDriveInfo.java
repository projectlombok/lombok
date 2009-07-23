package lombok.installer;

import java.util.ArrayList;
import java.util.List;

public class WindowsDriveInfo {
	public List<String> getLogicalDrives() {
		int flags = getLogicalDrives0();
		flags = (flags & 0xFF) << 24 |
				((flags >> 8) & 0xFF) << 16 |
				((flags >> 16) & 0xFF) << 8 |
				(flags >> 24) & 0xFF;
		
		List<String> letters = new ArrayList<String>();
		for ( int i = 0 ; i < 26 ; i++ ) {
			if ( (flags & (1 << i)) != 0 ) letters.add(Character.toString((char)('A' + i)));
		}
		
		return letters;
	}
	
	private native int getLogicalDrives0();
	
	public boolean isFixedDisk(String letter) {
		return getDriveType(letter.toUpperCase() + ":\\") == 3L;
	}
	
	private native int getDriveType(String name);
}
