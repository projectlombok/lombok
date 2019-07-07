
class SafeCallError {
	public SafeCallError() {
		String s = new UnexistedClass().getNullString().trim();
	}
}
