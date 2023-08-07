class ExtensionMethodWidening {
	public void test() {
		String string = "test";
		ExtensionMethodWidening.Extensions.widening(string, 1);
	}


	static class Extensions {
		public static String widening(String string, long a) {
			return string + " " + a;
		}
	}
}