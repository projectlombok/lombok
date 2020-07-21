import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.PostGeneratedConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
class PostGeneratedConstructorTest {
	private String name;
	private final Integer start;
	private final Integer end;
	
	@PostGeneratedConstructor
	private void validate() {
		if (start == null || end == null || end < start) {
			throw new IllegalArgumentException();
		}
	}
}