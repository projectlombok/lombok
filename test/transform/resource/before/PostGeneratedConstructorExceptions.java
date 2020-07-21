import lombok.AllArgsConstructor;
import lombok.experimental.PostGeneratedConstructor;

@AllArgsConstructor
class PostGeneratedConstructorExceptions {
	
	private String a;
	
	PostGeneratedConstructorExceptions() {
		
	}
	
	@PostGeneratedConstructor
	private void post() throws IllegalArgumentException {

	}
	
	@lombok.experimental.PostGeneratedConstructor
	private void post2() throws InterruptedException {
		
	}
	
}