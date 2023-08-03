// version 8:
import java.util.function.Function;

public class ExtensionMethodInLambda {
	public void testSimple() {
		String test = "test";
		test = ExtensionMethodInLambda.Extensions.map(test, s -> ExtensionMethodInLambda.Extensions.reverse(s));
	}

	public void testSameName() {
		String test = "test";
		test = ExtensionMethodInLambda.Extensions.map(test, s -> s.trim());
	}

	public void testArgumentOfInvalidMethod() {
		String test = "test";
		test.invalid(s -> s.reverse());
	}


	static class Extensions {
		public static <T, R> R map(T value, Function<T, R> mapper) {
			return mapper.apply(value);
		}

		public static String reverse(String string) {
			return new StringBuilder(string).reverse().toString();
		}

		public static String trim(Integer integer) {
			return "0";
		}
	}
}
