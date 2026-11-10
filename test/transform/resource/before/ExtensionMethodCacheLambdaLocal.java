// version 10:
import java.util.List;
import java.util.Optional;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(ExtensionMethodCacheLambdaLocal.Extensions.class)
class ExtensionMethodCacheLambdaLocal {
	static class Foo {
	}

	static class Bar {
	}

	public int test(List<String> list, Optional<String> opt) {
		int a = list.stream().mapToInt(x -> x.toFoo().toBar().count()).sum();
		int b = opt.map(x -> x.toFoo()).map(f -> f.toBar()).map(r -> r.count()).orElse(0);
		int c = Optional.of("x".toFoo()).map(f -> f.toBar().count()).orElse(0);
		int d = list.stream().mapToInt(x -> {
			var f = x.toFoo();
			var r = f.toBar();
			return r.count();
		}).sum();
		Optional<Integer> e = Optional.of("x".toFoo()).map(f -> f.toBar().count() + "lit".toBar().count());
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
