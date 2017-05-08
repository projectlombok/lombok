package first.second;
import lombok.experimental.SafeCall;

class SafeCallInPackage {
	
	public SafeCallInPackage() {
		@SafeCall String s = new first.second.SafeCallInPackage().getNullString().trim();
	}
	
	public String getNullString() {
		return null;
	}

}