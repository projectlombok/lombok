class LockedGeneratedTypeMismatch {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private final java.util.concurrent.locks.Lock $lock = new java.util.concurrent.locks.ReentrantLock();
	void test() {
		this.$lock.lock();
		try {
			System.out.println("one");
		} finally {
			this.$lock.unlock();
		}
	}
	void test2() {
		System.out.println("two");
	}
}
class LockedUserTypeMismatch {
	private final java.util.concurrent.locks.Lock userLock = new java.util.concurrent.locks.ReentrantLock();
	void test() {
		this.userLock.lock();
		try {
			System.out.println("one");
		} finally {
			this.userLock.unlock();
		}
	}
	void test2() {
		this.userLock.readLock().lock();
		try {
			System.out.println("two");
		} finally {
			this.userLock.readLock().unlock();
		}
	}
}
