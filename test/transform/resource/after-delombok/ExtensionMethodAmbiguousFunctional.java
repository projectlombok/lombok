//skip-idempotent
import java.util.function.Consumer;
import java.util.function.Function;

class ExtensionMethodAmbiguousFunctional {
	public void test() {
		ExtensionMethodAmbiguousFunctional.Extensions.ambiguous("", System.out::println);
	}

	static class Extensions {
		public static <T, R> void ambiguous(T t, Function<T, R> function) {
		}

		public static <T> void ambiguous(T t, Consumer<T> function) {
		}
	}
}
