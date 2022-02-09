import lombok.AllArgsConstructor;
import lombok.experimental.PostConstructor;

@AllArgsConstructor
class PostConstructorExceptions {
	
	private String a;
	
	PostConstructorExceptions() {
		
	}
	
	@PostConstructor
	private void post() throws IllegalArgumentException {

	}
	
	@lombok.experimental.PostConstructor
	private void post2() throws InterruptedException, java.lang.IllegalArgumentException, IllegalArgumentException {
		
	}
}