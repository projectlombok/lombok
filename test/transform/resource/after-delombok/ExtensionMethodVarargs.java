class ExtensionMethodVarargs {
	public void test() {
		Long l1 = 1L;
		long l2 = 1L;
		Integer i1 = 1;
		int i2 = 1;
		ExtensionMethodVarargs.Extensions.format("%d %d %d %d", l1, l2, i1, i2);
		ExtensionMethodVarargs.Extensions.format("%d", l1);
		ExtensionMethodVarargs.Extensions.format("", new Integer[] {1, 2});
		ExtensionMethodVarargs.Extensions.format("", new Integer[] {1, 2}, new Integer[] {1, 2});
	}


	static class Extensions {
		public static String format(String string, Object... params) {
			return String.format(string, params);
		}
	}
}