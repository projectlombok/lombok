package pkg;

import lombok.Locked;

import java.util.List;

public class LockedFolding {
	@Locked
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