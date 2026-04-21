package pkg;

import java.util.List;

import lombok.NonNull;

public class NonNullFolding {
	public void method(@NonNull List<String> a) {
		if (a.isEmpty()) {
			return;
		}
		
		try {
			
		} catch (Exception e) {
			
		} finally {
			
		}
	}
}