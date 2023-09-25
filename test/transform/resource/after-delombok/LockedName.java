import java.util.concurrent.locks.*;
class LockedName {
	private final Lock basicLock = new ReentrantLock();
	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
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
