class ExtensionMethodCacheChain {
	static class Base {
		Base self() {
			return this;
		}
		Number get() {
			return 1;
		}
	}
	static class Child extends Base {
	}
	public void test() {
		Base base = new Base();
		ExtensionMethodCacheChain.Extensions.childOnly(ExtensionMethodCacheChain.Extensions.self(base));
		ExtensionMethodCacheChain.Extensions.childOnly(ExtensionMethodCacheChain.Extensions.self(ExtensionMethodCacheChain.Extensions.self(base)));
		ExtensionMethodCacheChain.Extensions.shout(ExtensionMethodCacheChain.Extensions.childOnly(ExtensionMethodCacheChain.Extensions.self(base)));
		Child child = ExtensionMethodCacheChain.Extensions.self(base);
		ExtensionMethodCacheChain.Extensions.shout(ExtensionMethodCacheChain.Extensions.childOnly(child));
		ExtensionMethodCacheChain.Extensions.pad(ExtensionMethodCacheChain.Extensions.get(base), ExtensionMethodCacheChain.Extensions.shout(ExtensionMethodCacheChain.Extensions.get(base)));
		ExtensionMethodCacheChain.Extensions.pad(ExtensionMethodCacheChain.Extensions.shout(ExtensionMethodCacheChain.Extensions.get(base)), ExtensionMethodCacheChain.Extensions.childOnly(ExtensionMethodCacheChain.Extensions.self(base)));
		ExtensionMethodCacheChain.Extensions.childOnly(ExtensionMethodCacheChain.Extensions.self(child));
	}
	static class Extensions {
		public static Child self(Base base) {
			return new Child();
		}
		public static String get(Base base) {
			return "";
		}
		public static String childOnly(Child child) {
			return "";
		}
		public static String shout(String s) {
			return s;
		}
		public static String pad(String s, String other) {
			return s + other;
		}
	}
}
