import lombok.RequiredArgsConstructor;
import lombok.experimental.PostConstructor;

@RequiredArgsConstructor
class PostConstructorTest {
	private String name;
	private final Integer start;
	private final Integer end;
	private Integer range;
	
	@PostConstructor
	private void validate() {
		if (start == null || end == null || end < start) {
			throw new IllegalArgumentException();
		}
	}
	@PostConstructor
	private void calculateRange() {
		range = end - start;
	}
}