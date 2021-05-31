class LockedGeneratedStaticMismatch {
	@lombok.experimental.Locked static void test() {
		System.out.println("one");
	}
	@lombok.experimental.Locked("LOCK") void test2() {
		System.out.println("two");
	}
}
class LockedUserStaticMismatch {
	private static final java.util.concurrent.locks.ReentrantLock userLock = new java.util.concurrent.locks.ReentrantLock();
	@lombok.experimental.Locked("userLock") static void test() {
		System.out.println("one");
	}
	@lombok.experimental.Locked("userLock") void test2() {
		System.out.println("two");
	}
}
