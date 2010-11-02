public class ValInFor {
	{
		val x = 10;
		val x2 = -1;
		val a = "Hello";
		for (val y = x, z = x2; y < 20; y++) {
			val q = y;
			val w = z;
			val v = a;
		}
	}
	
/*	public void enhancedFor() {
		java.util.List<String> list = java.util.Arrays.asList("Hello, World!");
		for (val shouldBeString : list) {
			System.out.println(shouldBeString.toLowerCase());
			val shouldBeString2 = shouldBeString;
		}
	}*/
}