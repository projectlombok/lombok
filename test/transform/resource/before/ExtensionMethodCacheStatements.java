// version 10:
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(ExtensionMethodCacheStatements.Extensions.class)
class ExtensionMethodCacheStatements {
	static class Foo {
	}

	static class Bar {
	}

	static final int TOTAL;
	static {
		var a = "a".toFoo();
		var b = a.toBar();
		var c = b.again();
		var d = c.toBar();
		TOTAL = d.count() + a.toBar().count();
	}

	static Function<String, Integer> function = x -> {
		var f = x.toFoo();
		return f.toBar().count();
	};

	static Supplier<Integer> supplier = () -> {
		var f = "x".toFoo();
		var b = f.toBar();
		return b.count();
	};

	int field = "a".toFoo().toBar().count();

	public int test(String s) {
		var a = s.toFoo();
		var b = a.toBar();
		var c = b.again();
		var d = c.toBar();
		var e = d.again();
		var f = e.toBar();
		return f.count() + d.count() + b.count();
	}

	static class Extensions {
		public static Foo toFoo(String s) {
			return new Foo();
		}

		public static Bar toBar(Foo foo) {
			return new Bar();
		}

		public static Foo again(Bar bar) {
			return new Foo();
		}

		public static int count(Bar bar) {
			return 1;
		}
	}
}
