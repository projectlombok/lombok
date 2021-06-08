@lombok.Data
public class SimpleTest {
	private final String test;
	private final int foo;
	private final boolean baz;

	public String bar() {
		int val = getFoo() + 5;
		return new SimpleTest("", 0, false).toString() + val;
	}
}
