import java.util.List;
import java.util.Map;

class ExtensionMethodGeneric {
	public void test() {
		List<String> stringList = null;
		List<Number> numberList = null;
		ExtensionMethodGeneric.Extensions.test(stringList);
		ExtensionMethodGeneric.Extensions.test(stringList, numberList);
		ExtensionMethodGeneric.Extensions.test(ExtensionMethodGeneric.Extensions.test(stringList, stringList), numberList);
		Integer i = ExtensionMethodGeneric.Extensions.test2(stringList);
		Map<String, Integer> map = null;
		List<String> l = ExtensionMethodGeneric.Extensions.test(map, stringList, numberList);
	}
	
	static class Extensions {
		public static <T> List<T> test(List<String> obj, List<T> list) {
			return null;
		}

		public static <K, V> K test(Map<String, Integer> obj, K k, V v) {
			return k;
		}

		public static <T> T test(List<T> list) {
			return null;
		}

		public static <T, U> U test2(List<T> list) {
			return null;
		}
	}
}
