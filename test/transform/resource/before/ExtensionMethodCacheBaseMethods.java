// version 10:
//platform javac: the Eclipse handler leaves a call that a base method accepts
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(value = ExtensionMethodCacheBaseMethods.Extensions.class, suppressBaseMethods = false)
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
		var n = s.toInt();
		return this.foo.pad(n) + this.foo.wrap(n);
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
