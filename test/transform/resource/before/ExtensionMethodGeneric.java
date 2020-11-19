import java.util.List;
import java.util.Map;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(ExtensionMethodGeneric.Extensions.class)
class ExtensionMethodGeneric {
	public void test() {
		List<String> stringList = null;
		List<Number> numberList = null;
		stringList.test();
		stringList.test(numberList);
		stringList.test(stringList).test(numberList);
		Integer i = stringList.test2();
		
		Map<String, Integer> map = null;
		List<String> l = map.test(stringList, numberList);
	}
	
	static class Extensions {
		public static <T> List<T> test(List<String> obj, List<T> list) {
			return null;
		}
		public static <K,V> K test(Map<String, Integer> obj, K k, V v) {
			return k;
		}
		public static <T> T test(List<T> list) {
			return null;
		}
		public static <T,U> U test2(List<T> list) {
			return null;
		}
	}
}
