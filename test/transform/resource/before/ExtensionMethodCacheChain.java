import lombok.experimental.ExtensionMethod;

@ExtensionMethod(ExtensionMethodCacheChain.Extensions.class)
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
		base.self().childOnly();
		base.self().self().childOnly();
		base.self().childOnly().shout();
		Child child = base.self();
		child.childOnly().shout();
		base.get().pad(base.get().shout());
		base.get().shout().pad(base.self().childOnly());
		child.self().childOnly();
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
