// version 10:
import java.io.IOException;
import java.util.Arrays;

public class ValToNative {
	private void test() throws IOException {
		final var intField = 1;
		for (final var s : Arrays.asList("1")) {
			final var s2 = s;
		}
		try (var in = getClass().getResourceAsStream("ValToNative.class")) {
			final var j = in.read();
		}
	}
}