import lombok.experimental.PostConstructor;

abstract class PostConstructorInvalid {
	@PostConstructor
	private static void staticMethod() {
	}
	
	@PostConstructor
	public abstract void abstractMethod();
	
	@PostConstructor
	private void withArgument(String arg) {
	}
}