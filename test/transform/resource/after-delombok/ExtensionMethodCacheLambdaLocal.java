// version 10:
import java.util.List;
import java.util.Optional;
class ExtensionMethodCacheLambdaLocal {
	static class Foo {
	}
	static class Bar {
	}
	public int test(List<String> list, Optional<String> opt) {
		int a = list.stream().mapToInt(x -> ExtensionMethodCacheLambdaLocal.Extensions.count(ExtensionMethodCacheLambdaLocal.Extensions.toBar(ExtensionMethodCacheLambdaLocal.Extensions.toFoo(x)))).sum();
		int b = opt.map(x -> ExtensionMethodCacheLambdaLocal.Extensions.toFoo(x)).map(f -> ExtensionMethodCacheLambdaLocal.Extensions.toBar(f)).map(r -> ExtensionMethodCacheLambdaLocal.Extensions.count(r)).orElse(0);
		int c = Optional.of(ExtensionMethodCacheLambdaLocal.Extensions.toFoo("x")).map(f -> ExtensionMethodCacheLambdaLocal.Extensions.count(ExtensionMethodCacheLambdaLocal.Extensions.toBar(f))).orElse(0);
		int d = list.stream().mapToInt(x -> {
			var f = ExtensionMethodCacheLambdaLocal.Extensions.toFoo(x);
			var r = ExtensionMethodCacheLambdaLocal.Extensions.toBar(f);
			return ExtensionMethodCacheLambdaLocal.Extensions.count(r);
		}).sum();
		Optional<Integer> e = Optional.of(ExtensionMethodCacheLambdaLocal.Extensions.toFoo("x")).map(f -> ExtensionMethodCacheLambdaLocal.Extensions.count(ExtensionMethodCacheLambdaLocal.Extensions.toBar(f)) + ExtensionMethodCacheLambdaLocal.Extensions.count(ExtensionMethodCacheLambdaLocal.Extensions.toBar("lit")));
		return a + b + c + d + e.get();
	}
	static class Extensions {
		public static Foo toFoo(String s) {
			return new Foo();
		}
		public static Bar toBar(Foo foo) {
			return new Bar();
		}
		public static Bar toBar(String s) {
			return new Bar();
		}
		public static int count(Bar bar) {
			return 1;
		}
	}
}
