class SynchronizedPlain1 {
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
	private final java.lang.Object $lock = new java.lang.Object[0];
}
class SynchronizedPlain2 {
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
	private static final java.lang.Object $LOCK = new java.lang.Object[0];
}