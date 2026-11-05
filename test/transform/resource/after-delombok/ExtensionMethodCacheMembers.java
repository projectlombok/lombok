class ExtensionMethodCacheMembers {
	static class Base {
		Number get() {
			return 1;
		}
	}
	static final String A = ExtensionMethodCacheMembers.Extensions.shout("a");
	String b = ExtensionMethodCacheMembers.Extensions.shout(ExtensionMethodCacheMembers.Extensions.twice(ExtensionMethodCacheMembers.Extensions.len(ExtensionMethodCacheMembers.Extensions.shout(A))));
	String c = ExtensionMethodCacheMembers.Extensions.shout(ExtensionMethodCacheMembers.Extensions.get(new Base()));
	String d = ExtensionMethodCacheMembers.Extensions.shout("d");
	static {
		String s = "x";
		ExtensionMethodCacheMembers.Extensions.shout(s);
		ExtensionMethodCacheMembers.Extensions.shout(ExtensionMethodCacheMembers.Extensions.get(new Base()));
		ExtensionMethodCacheMembers.Extensions.shout(s);
	}
	{
		String s = "y";
		ExtensionMethodCacheMembers.Extensions.shout(s);
		ExtensionMethodCacheMembers.Extensions.twice(ExtensionMethodCacheMembers.Extensions.len(s));
	}
	public void one() {
		ExtensionMethodCacheMembers.Extensions.shout(ExtensionMethodCacheMembers.Extensions.get(new Base()));
		ExtensionMethodCacheMembers.Extensions.len(ExtensionMethodCacheMembers.Extensions.shout("1"));
	}
	public void two() {
		ExtensionMethodCacheMembers.Extensions.twice(ExtensionMethodCacheMembers.Extensions.len(ExtensionMethodCacheMembers.Extensions.shout("2")));
	}
	public void three() {
		class Local {
			String v = ExtensionMethodCacheMembers.Extensions.shout("3");
			void run() {
				ExtensionMethodCacheMembers.Extensions.shout(ExtensionMethodCacheMembers.Extensions.get(new Base()));
				ExtensionMethodCacheMembers.Extensions.shout(v);
			}
		}
		ExtensionMethodCacheMembers.Extensions.shout("3");
	}
	static class Nested {
		String e = ExtensionMethodCacheMembers.Extensions.shout("e");
		void run() {
			ExtensionMethodCacheMembers.Extensions.len(ExtensionMethodCacheMembers.Extensions.shout("n"));
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
