import lombok.experimental.SafeCall;

class SafeCallDefaultPackage {
	
	public SafeCallDefaultPackage() {
		@SafeCall String s = new SafeCallDefaultPackage().getNullString().trim();
	}
	
	public String getNullString() {
		return null;
	}

}