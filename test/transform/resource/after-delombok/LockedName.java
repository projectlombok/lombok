class LockedName {
	@java.lang.SuppressWarnings("all")
	private final java.util.concurrent.locks.ReentrantLock basicLock = new java.util.concurrent.locks.ReentrantLock();
	@java.lang.SuppressWarnings("all")
	private final java.util.concurrent.locks.ReentrantReadWriteLock rwLock = new java.util.concurrent.locks.ReentrantReadWriteLock();

	void test() {
		this.basicLock.lock();
		try {
			System.out.println("one");
		} finally {
			this.basicLock.unlock();
		}
	}
	void test2() {
		this.rwLock.readLock().lock();
		try {
			System.out.println("two");
		} finally {
			this.rwLock.readLock().unlock();
		}
	}
	void test3() {
		this.rwLock.writeLock().lock();
		try {
			System.out.println("three");
		} finally {
			this.rwLock.writeLock().unlock();
		}
	}
}
