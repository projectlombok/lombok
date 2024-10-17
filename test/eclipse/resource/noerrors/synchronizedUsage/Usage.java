package pkg;

public class Usage {
	private ClassWithSynchronized test;
	
	// Prevent "The value of the field Usage.test is not used"
	public void test() {
		test.test();
	}
}