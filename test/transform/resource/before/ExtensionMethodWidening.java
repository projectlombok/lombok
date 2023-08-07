import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ExtensionMethodWidening.Extensions.class})
class ExtensionMethodWidening {
	public void test() {
		String string = "test";
		string.widening(1);
	}
	
	static class Extensions {
		public static String widening(String string, long a) {
			return string + " " + a;
		}
	}
}
