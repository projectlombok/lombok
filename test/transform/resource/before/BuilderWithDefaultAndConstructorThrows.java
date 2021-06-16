import lombok.Builder;
import java.io.IOException;

@Builder
public class BuilderWithDefaultAndConstructorThrows {
	@Builder.Default
	private final int value = 10;

	@Builder
	public BuilderWithDefaultAndConstructorThrows(int value) throws IOException {
		if (value > 100) {
			throw new IOException("value too large");
		}
		this.value = value;
	}
}
