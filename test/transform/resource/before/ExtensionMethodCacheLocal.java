// version 10:
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(ExtensionMethodCacheLocal.Extensions.class)
class ExtensionMethodCacheLocal {
	static class Foo {
	}

	static class Bar {
	}

	public int test(String s) {
		Foo f = s.toFoo();
		Bar b = f.toBar();
		var g = s.toFoo();
		var c = g.toBar();
		int n = c.count() + b.count();
		Foo h;
		h = c.again();
		var d = h.toBar();
		if (n > 0) {
			var e = d.again();
			n += e.toBar().count();
		}
		return n + h.toBar().count();
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
