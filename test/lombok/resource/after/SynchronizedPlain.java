class SynchronizedPlain1 {
	private final Object $lock = new Object[0];
	void test() {
		synchronized ($lock) {
			System.out.println("one");
		}
	}
	void test2() {
		synchronized ($lock) {
			System.out.println("two");
		}
	}
}
class SynchronizedPlain2 {
	private static final Object $LOCK = new Object[0];
	static void test() {
		synchronized ($LOCK) {
			System.out.println("three");
		}
	}
	static void test2() {
		synchronized ($LOCK) {
			System.out.println("four");
		}
	}
}