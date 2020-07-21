import lombok.experimental.PostGeneratedConstructor;

abstract class PostGeneratedConstructorInvalid {
	@PostGeneratedConstructor
	private void noConstructor() {

	}
	
	@PostGeneratedConstructor
	private static void staticMethod() {

	}
	
	@PostGeneratedConstructor
	public abstract void abstractMethod();
}