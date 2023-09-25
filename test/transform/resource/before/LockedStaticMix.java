class LockedGeneratedStaticMismatch {
	@lombok.Locked static void test() {
		System.out.println("one");
	}
	@lombok.Locked("$LOCK") void test2() {
		System.out.println("two");
	}
}
class LockedUserStaticMismatch {
	private static final java.util.concurrent.locks.Lock userLock = new java.util.concurrent.locks.ReentrantLock();
	@lombok.Locked("userLock") static void test() {
		System.out.println("one");
	}
	@lombok.Locked("userLock") void test2() {
		System.out.println("two");
	}
}
