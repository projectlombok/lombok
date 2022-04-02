// version 8:
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ExtensionMethodFunctional {
	public void test() {
		String test = "test";
		test = ExtensionMethodFunctional.Extensions.map(test, s -> ExtensionMethodFunctional.Extensions.reverse(s));
		ExtensionMethodFunctional.Extensions.consume(test, s -> System.out.println("1: " + s), s -> System.out.println("2: " + s));
		ExtensionMethodFunctional.Extensions.consume(test, System.out::println, System.out::println);
		ExtensionMethodFunctional.Extensions.consume(test, test.length() > 0 ? System.out::println : null);
		ExtensionMethodFunctional.Extensions.toList1(Stream.of("a", "b", "c").map(String::toUpperCase));
		List<Integer> i2 = ExtensionMethodFunctional.Extensions.toList2(Stream.of("a", "b", "c").map(String::toUpperCase));
	}

	static class Extensions {
		public static <T, R> R map(T value, Function<T, R> mapper) {
			return mapper.apply(value);
		}

		public static String reverse(String string) {
			return new StringBuilder(string).reverse().toString();
		}

		@SafeVarargs
		public static <T> void consume(T o, Consumer<T>... consumer) {
			for (int i = 0; i < consumer.length; i++) {
				consumer[i].accept(o);
			}
		}
		
		public static <T> List<T> toList1(Stream<T> stream) {
			return (List<T>) stream.collect(Collectors.toList());
		}

		public static <T, U> List<U> toList2(Stream<T> stream) {
			return null;
		}
	}
}
