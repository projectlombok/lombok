import java.util.List;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(ExtensionMethodCacheGeneric.Extensions.class)
class ExtensionMethodCacheGeneric {
	public void test() {
		List<String> strings = null;
		List<Number> numbers = null;
		"a".shout().len();
		strings.first().shout();
		strings.first().shout().len();
		numbers.first().twice();
		"b".shout().wrap().first().shout();
		strings.first().len().twice().wrap().first();
		"c".shout();
		numbers.first().twice().twice();
	}

	static class Extensions {
		public static <T> T first(List<T> list) {
			return list.isEmpty() ? null : list.get(0);
		}

		public static <T> List<T> wrap(T value) {
			return null;
		}

		public static String shout(String s) {
			return s;
		}

		public static Integer len(String s) {
			return s.length();
		}

		public static Integer twice(Number n) {
			return n.intValue() * 2;
		}
	}
}
