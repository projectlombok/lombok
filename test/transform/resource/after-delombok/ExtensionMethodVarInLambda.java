//version :9
import java.util.function.Function;

public class ExtensionMethodVarInLambda {
	public void test() {
		String test = "test";
		ExtensionMethodVarInLambda.Extensions.map(test, s -> {
			java.lang.String reversed = ExtensionMethodVarInLambda.Extensions.reverse(s);
			return reversed;
		});
	}


	static class Extensions {
		public static <T, R> R map(T value, Function<T, R> mapper) {
			return mapper.apply(value);
		}

		public static String reverse(String string) {
			return new StringBuilder(string).reverse().toString();
		}
	}
}
