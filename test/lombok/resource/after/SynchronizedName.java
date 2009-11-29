class SynchronizedName {
	private Object read = new Object();
	private static Object READ = new Object();
	void test1() {
		synchronized (read) {
			System.out.println("one");
		}
	}
	void test2() {
		System.out.println("two");
	}
	static void test3() {
		System.out.println("three");
	}
	void test4() {
		synchronized (READ) {
			System.out.println("four");
		}
	}
}
