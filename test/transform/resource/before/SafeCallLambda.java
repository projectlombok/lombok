// version 8:
import lombok.experimental.SafeCall;

class SafeCallLambda {
	public SafeCallLambda() {
		super();
		@SafeCall Runnable r = () -> {
			@SafeCall int i = getNullInteger();
		};
	}

	public Integer getNullInteger() {
		return null;
	}
}