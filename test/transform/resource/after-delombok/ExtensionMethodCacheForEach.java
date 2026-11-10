// version 10:
import java.util.Collections;
import java.util.List;
class ExtensionMethodCacheForEach {
	static class Foo {
	}
	static class Bar {
	}
	public int test(String s) {
		int n = 0;
		for (Foo f : ExtensionMethodCacheForEach.Extensions.toFoos(s)) n += ExtensionMethodCacheForEach.Extensions.count(ExtensionMethodCacheForEach.Extensions.toBar(f));
		for (var f : ExtensionMethodCacheForEach.Extensions.toFoos(s)) n += ExtensionMethodCacheForEach.Extensions.count(ExtensionMethodCacheForEach.Extensions.toBar(f));
		for (var f : ExtensionMethodCacheForEach.Extensions.toFoos(s)) {
			var b = ExtensionMethodCacheForEach.Extensions.toBar(f);
			for (var g : ExtensionMethodCacheForEach.Extensions.again(b)) n += ExtensionMethodCacheForEach.Extensions.count(ExtensionMethodCacheForEach.Extensions.toBar(g));
		}
		return n;
	}
	static class Extensions {
		public static List<Foo> toFoos(String s) {
			return Collections.singletonList(new Foo());
		}
		public static Bar toBar(Foo foo) {
			return new Bar();
		}
		public static List<Foo> again(Bar bar) {
			return Collections.singletonList(new Foo());
		}
		public static int count(Bar bar) {
			return 1;
		}
	}
}
