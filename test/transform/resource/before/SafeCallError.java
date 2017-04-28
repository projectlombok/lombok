import lombok.experimental.SafeCall;

class SafeCallError {
	
	public SafeCallError() {
		@SafeCall String s = new UnexistedClass().getNullString().trim();
	}
	
}
