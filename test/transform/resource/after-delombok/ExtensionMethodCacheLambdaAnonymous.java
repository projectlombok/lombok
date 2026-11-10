// version 8:
import java.util.function.Function;
class ExtensionMethodCacheLambdaAnonymous {
	static class Base {
		Number get() {
			return 1;
		}
	}
	public void test() {
		Base base = new Base();
		String s = "a";
		ExtensionMethodCacheLambdaAnonymous.Extensions.shout(s);
		Function<String, String> f = t -> ExtensionMethodCacheLambdaAnonymous.Extensions.shout(t);
		Function<Base, String> g = b -> ExtensionMethodCacheLambdaAnonymous.Extensions.shout(ExtensionMethodCacheLambdaAnonymous.Extensions.get(b));
		Runnable r = new Runnable() {
			public void run() {
				String u = "b";
				ExtensionMethodCacheLambdaAnonymous.Extensions.shout(ExtensionMethodCacheLambdaAnonymous.Extensions.shout(u));
				ExtensionMethodCacheLambdaAnonymous.Extensions.shout(ExtensionMethodCacheLambdaAnonymous.Extensions.get(base));
			}
			void other() {
				ExtensionMethodCacheLambdaAnonymous.Extensions.shout("c");
			}
		};
		ExtensionMethodCacheLambdaAnonymous.Extensions.shout(s);
		ExtensionMethodCacheLambdaAnonymous.Extensions.shout(ExtensionMethodCacheLambdaAnonymous.Extensions.get(base));
		Function<Base, Integer> h = b -> ExtensionMethodCacheLambdaAnonymous.Extensions.length(ExtensionMethodCacheLambdaAnonymous.Extensions.get(b));
		ExtensionMethodCacheLambdaAnonymous.Extensions.shout(s);
	}
	static class Extensions {
		public static String get(Base base) {
			return "";
		}
		public static String shout(String s) {
			return s;
		}
		public static Integer length(String s) {
			return s.length();
		}
	}
}
