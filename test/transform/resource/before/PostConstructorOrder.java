import lombok.experimental.PostConstructor;

class PostConstructorOrder {
	@PostConstructor
	private void first() {
	}
	
	@PostConstructor
	private void second() {
	}
	
	@PostConstructor
	private void third() {
	}
	
	@PostConstructor
	private void fourth() {
	}
}