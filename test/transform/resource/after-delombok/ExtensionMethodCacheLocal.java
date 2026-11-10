// version 10:
class ExtensionMethodCacheLocal {
	static class Foo {
	}
	static class Bar {
	}
	public int test(String s) {
		Foo f = ExtensionMethodCacheLocal.Extensions.toFoo(s);
		Bar b = ExtensionMethodCacheLocal.Extensions.toBar(f);
		var g = ExtensionMethodCacheLocal.Extensions.toFoo(s);
		var c = ExtensionMethodCacheLocal.Extensions.toBar(g);
		int n = ExtensionMethodCacheLocal.Extensions.count(c) + ExtensionMethodCacheLocal.Extensions.count(b);
		Foo h;
		h = ExtensionMethodCacheLocal.Extensions.again(c);
		var d = ExtensionMethodCacheLocal.Extensions.toBar(h);
		if (n > 0) {
			var e = ExtensionMethodCacheLocal.Extensions.again(d);
			n += ExtensionMethodCacheLocal.Extensions.count(ExtensionMethodCacheLocal.Extensions.toBar(e));
		}
		return n + ExtensionMethodCacheLocal.Extensions.count(ExtensionMethodCacheLocal.Extensions.toBar(h));
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
