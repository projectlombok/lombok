import lombok.experimental.ExtensionMethod;

@ExtensionMethod(ExtensionMethodCacheReceiver.Extensions.class)
class ExtensionMethodCacheReceiver {
	static class Foo {
	}

	static class Bar {
	}

	String text = "";

	public int test() {
		int a = text.toFoo().toBar().count();
		int b = "x".toFoo().toBar().again().toBar().count();
		int c = Math.max(this.text.toFoo().toBar().count(), text.toFoo().toBar().again().toBar().count());
		return a + b + c + text.toFoo().toBar().again().toBar().again().toBar().count();
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
