public class Lambda {
	Runnable r = () -> System.out.println();
	java.util.Comparator<Integer> c1 = (a, b) -> a - b;
	java.util.Comparator<Integer> c2 = (Integer a, Integer b) -> {
		return a - b;
	};
	java.util.function.Function<String, String> fnc = (String c) -> c;
	void testLambdaInArgsList(String name, java.util.function.Function<String, String> f) {
	}
	void testLambdaInArgsList2(java.util.function.Function<String, String> f, String name) {
	}
	void test() {
		testLambdaInArgsList("hello", (String c) -> c);
		testLambdaInArgsList("hello", c -> c);
		testLambdaInArgsList2((String c) -> c, "hello");
		testLambdaInArgsList2(c -> c, "hello");
	}
}
