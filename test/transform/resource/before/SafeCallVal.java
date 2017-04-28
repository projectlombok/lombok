import lombok.experimental.SafeCall;
import lombok.val;

class SafeCallVal {
	
	public SafeCallVal() {
		@SafeCall val s = new SafeCallVal().getNullString().trim();
		@SafeCall val nullSafeCall = new SafeCallVal().getNullSafeCall();
		@SafeCall val s1 = nullSafeCall.getNullString().trim();
	}
	
	public SafeCallVal nullSafeCall;
	
	public String getNullString() {
		return null;
	}
	
	public SafeCallVal getNullSafeCall() {
		return null;
	}
}