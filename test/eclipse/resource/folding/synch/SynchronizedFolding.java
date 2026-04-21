package pkg;

import lombok.Synchronized;

import java.util.List;

public class LockedFolding {
	@Synchronized
	public void method() {
		List<String> a = null;
		
		if (a.isBlank()) {
			return;
		}
		
		try {
			
		} catch (Exception e) {
			
		} finally {
			
		}
	}
}