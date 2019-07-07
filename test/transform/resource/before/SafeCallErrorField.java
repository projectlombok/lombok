//unchanged

import lombok.experimental.SafeCall;

class SafeCallErrorField {
	@SafeCall
	String s = new UnexistedClass().getNullString().trim();
}
