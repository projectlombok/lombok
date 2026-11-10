import lombok.experimental.ExtensionMethod;

@ExtensionMethod(ExtensionMethodCacheTypeChange.Extensions.class)
class ExtensionMethodCacheTypeChange {
	static class Base {
		Number get() {
			return 1;
		}

		String text() {
			return "";
		}

		String count() {
			return "";
		}
	}

	public void test() {
		Base base = new Base();
		base.text().shout();
		base.get().shout();
		base.text().shout();
		base.count().half();
		base.count().pad();
		base.get().pad();
		base.text().shout();
	}

	static class Extensions {
		public static String get(Base base) {
			return "";
		}

		public static String text(Base base) {
			return base.toString();
		}

		public static Integer count(Base base) {
			return 0;
		}

		public static String shout(String s) {
			return s;
		}

		public static Integer half(Integer i) {
			return i;
		}

		public static String pad(String s) {
			return s;
		}

		public static Integer pad(Integer i) {
			return i;
		}
	}
}
