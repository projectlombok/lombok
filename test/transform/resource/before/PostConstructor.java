import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.PostConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
class PostConstructorTest {
	private String name;
	private final Integer start;
	private final Integer end;
	
	@PostConstructor
	private void validate() {
		if (start == null || end == null || end < start) {
			throw new IllegalArgumentException();
		}
	}
}