import lombok.experimental.ExtensionMethod;

@ExtensionMethod(ExtensionMethodCacheMembers.Extensions.class)
class ExtensionMethodCacheMembers {
	static class Base {
		Number get() {
			return 1;
		}
	}

	static final String A = "a".shout();
	String b = A.shout().len().twice().shout();
	String c = new Base().get().shout();
	String d = "d".shout();

	static {
		String s = "x";
		s.shout();
		new Base().get().shout();
		s.shout();
	}

	{
		String s = "y";
		s.shout();
		s.len().twice();
	}

	public void one() {
		new Base().get().shout();
		"1".shout().len();
	}

	public void two() {
		"2".shout().len().twice();
	}

	public void three() {
		class Local {
			String v = "3".shout();

			void run() {
				new Base().get().shout();
				v.shout();
			}
		}
		"3".shout();
	}

	static class Nested {
		String e = "e".shout();

		void run() {
			"n".shout().len();
		}
	}

	static class Extensions {
		public static String get(Base base) {
			return "";
		}

		public static String shout(String s) {
			return s;
		}

		public static Integer len(String s) {
			return s.length();
		}

		public static String twice(Number n) {
			return "";
		}
	}
}
