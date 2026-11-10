// version 10:
import java.util.Collections;
import java.util.List;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod(ExtensionMethodCacheForEach.Extensions.class)
class ExtensionMethodCacheForEach {
	static class Foo {
	}

	static class Bar {
	}

	public int test(String s) {
		int n = 0;
		for (Foo f : s.toFoos()) n += f.toBar().count();
		for (var f : s.toFoos()) n += f.toBar().count();
		for (var f : s.toFoos()) {
			var b = f.toBar();
			for (var g : b.again()) n += g.toBar().count();
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
