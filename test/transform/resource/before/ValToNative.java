// version 10:
import java.io.IOException;
import java.util.Arrays;

import lombok.val;

public class ValToNative {
	private void test() throws IOException {
		val intField = 1;
		
		for (val s : Arrays.asList("1")) {
			val s2 = s;
		}
		
		try (val in = getClass().getResourceAsStream("ValToNative.class")) {
			val j = in.read();
		}
	}
}
