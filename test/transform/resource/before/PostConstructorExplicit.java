import lombok.experimental.PostConstructor;

class PostConstructorExplicit {
	@PostConstructor
	private void postConstructor1() {
	}
	
	@PostConstructor
	private void postConstructor2() {
	}
	
	public PostConstructorExplicit(long a) {
		this();
	}
	
	@PostConstructor.SkipPostConstructors
	public PostConstructorExplicit(int a) {
	}
	
	@PostConstructor.InvokePostConstructors
	public PostConstructorExplicit(short a) {
	}
	
	@PostConstructor.SkipPostConstructors
	@PostConstructor.InvokePostConstructors
	public PostConstructorExplicit(byte a) {
	}
	
	@PostConstructor.InvokePostConstructors
	public PostConstructorExplicit(boolean a) {
		this();
	}
	
	@PostConstructor.SkipPostConstructors
	public PostConstructorExplicit(double a) {
		this();
	}
	
	public PostConstructorExplicit() {
	}
}