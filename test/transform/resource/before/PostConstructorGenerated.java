import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.PostConstructor;

@NoArgsConstructor
class PostConstructorGeneratedNoArgs {
	private String a;
	private String b;
	
	@PostConstructor
	private void postConstructor() {
	}
}
@RequiredArgsConstructor
class PostConstructorGeneratedRequiredArgs {
	private final String a;
	private final String b;
	
	@PostConstructor
	private void postConstructor() {
	}
}
@AllArgsConstructor
class PostConstructorGeneratedAllArgs {
	private String a;
	private String b;
	
	@PostConstructor
	private void postConstructor() {
	}
}