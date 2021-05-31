class LockedGeneratedStaticMismatch {
	@java.lang.SuppressWarnings("all")
	private static final java.util.concurrent.locks.ReentrantLock LOCK = new java.util.concurrent.locks.ReentrantLock();
	static void test() {
		LockedGeneratedStaticMismatch.LOCK.lock();
		try {
			System.out.println("one");
		} finally {
			LockedGeneratedStaticMismatch.LOCK.unlock();
		}
	}
	void test2() {
		System.out.println("two");
	}
}
class LockedUserStaticMismatch {
	private static final java.util.concurrent.locks.ReentrantLock userLock = new java.util.concurrent.locks.ReentrantLock();
	static void test() {
		LockedUserStaticMismatch.userLock.lock();
		try {
			System.out.println("one");
		} finally {
			LockedUserStaticMismatch.userLock.unlock();
		}
	}
	void test2() {
		LockedUserStaticMismatch.userLock.lock();
		try {
			System.out.println("two");
		} finally {
			LockedUserStaticMismatch.userLock.unlock();
		}
	}
}
