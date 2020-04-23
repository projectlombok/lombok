class ExtensionMethodAutoboxing {
	public void test() {
		Long l1 = 1L;
		long l2 = 1L;
		Integer i1 = 1;
		int i2 = 1;
		String string = "test";
		ExtensionMethodAutoboxing.Extensions.boxing(string, l1, i1);
		ExtensionMethodAutoboxing.Extensions.boxing(string, l1, i2);
		ExtensionMethodAutoboxing.Extensions.boxing(string, l2, i1);
		ExtensionMethodAutoboxing.Extensions.boxing(string, l2, i2);
	}


	static class Extensions {
		public static String boxing(String string, Long a, int b) {
			return string + " " + a + " " + b;
		}
	}
}