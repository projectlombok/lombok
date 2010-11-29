public class ValInFor {
	{
		final int x = 10;
		final int x2 = -1;
		final java.lang.String a = "Hello";
		for (final int y = x, z = x2; y < 20; y++) {
			final int q = y;
			final int w = z;
			final java.lang.String v = a;
		}
	}
	public void enhancedFor() {
		java.util.List<String> list = java.util.Arrays.asList("Hello, World!");
		for (final java.lang.String shouldBeString : list) {
			System.out.println(shouldBeString.toLowerCase());
			final java.lang.String shouldBeString2 = shouldBeString;
		}
	}
}