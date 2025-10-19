import lombok.Builder;
import lombok.experimental.PostConstructor;

@Builder
class PostConstructorBuilder {
	private String field;
	
	@PostConstructor
	private void postConstructor() {
	}
}