package pkg;

import lombok.Cleanup;

import java.util.List;

public class CleanupFolding {
	public void method() {
		@Cleanup FileInputStream inStream = new FileInputStream("test");
		
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