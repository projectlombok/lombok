// version 8:
import java.util.function.Function;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(ExtensionMethodCacheLambdaAnonymous.Extensions.class)
class ExtensionMethodCacheLambdaAnonymous {
	static class Base {
		Number get() {
			return 1;
		}
	}

	public void test() {
		Base base = new Base();
		String s = "a";
		s.shout();
		Function<String, String> f = t -> t.shout();
		Function<Base, String> g = b -> b.get().shout();
		Runnable r = new Runnable() {
			public void run() {
				String u = "b";
				u.shout().shout();
				base.get().shout();
			}

			void other() {
				"c".shout();
			}
		};
		s.shout();
		base.get().shout();
		Function<Base, Integer> h = b -> b.get().length();
		s.shout();
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
