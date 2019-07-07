//unchanged

import lombok.experimental.SafeCall;

class SafeCallError2 {
	
	public SafeCallError2() {
		@SafeCall String s = unexistedField.getNullString().trim();
	}
}
