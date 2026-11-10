// version 10:
class ExtensionMethodCacheBaseMethods {
	static class Foo {
		String pad(int n) {
			return "";
		}
		String wrap(String s) {
			return s;
		}
	}
	Foo foo = new Foo();
	public String test(String s) {
		var n = ExtensionMethodCacheBaseMethods.Extensions.toInt(s);
		return this.foo.pad(n) + ExtensionMethodCacheBaseMethods.Extensions.wrap(this.foo, n);
	}
	static class Extensions {
		public static int toInt(String s) {
			return 1;
		}
		public static String pad(Foo foo, int n) {
			return "";
		}
		public static String wrap(Foo foo, int n) {
			return "";
		}
	}
}
