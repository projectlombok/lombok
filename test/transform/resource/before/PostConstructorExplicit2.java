// version 8:
import lombok.experimental.PostConstructor;
import lombok.experimental.PostConstructor.InvokePostConstructors;

class PostConstructorExplicit2 {
	@PostConstructor
	private void first() {
	}
	
	@PostConstructor
	private void second() {
	}
	
	@InvokePostConstructors
	public PostConstructorExplicit2(long tryFinally) throws java.io.IOException {
		if ("a".equals("b")) {
			return;
		}
		if ("a".equals("b")) {
			throw new java.io.IOException("");
		}
	}
	@InvokePostConstructors
	public PostConstructorExplicit2(int innerExit) {
		Runnable r1 = new Runnable() {
			public void run() {
				return;
			}
		};
	}
	@InvokePostConstructors
	public PostConstructorExplicit2(short lambdaExit) {
		Runnable r2 = () -> {
			return;
		};
	}
}