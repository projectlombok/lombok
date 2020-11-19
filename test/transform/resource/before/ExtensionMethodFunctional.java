// version 8:
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(value = ExtensionMethodFunctional.Extensions.class, suppressBaseMethods = false)
class ExtensionMethodFunctional {
	public void test() {
		String test = "test";
		test = test.map(s -> s.reverse());
		
		test.consume(s -> System.out.println("1: " + s), s -> System.out.println("2: " + s));
		test.consume(System.out::println, System.out::println);
		
		Stream.of("a", "b", "c").map(String::toUpperCase).toList();
		List<Integer> i2 = Stream.of("a", "b", "c").map(String::toUpperCase).toList2();
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
		
		public static <T> List<T> toList(Stream<T> stream) {
			return (List<T>) stream.collect(Collectors.toList());
		}
		
		public static <T, U> List<U> toList2(Stream<T> stream) {
			return null;
		}
	}
}
