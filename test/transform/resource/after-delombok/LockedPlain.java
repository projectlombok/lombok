class LockedPlain {
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
		this.$lock.lock();
		try {
			System.out.println("two");
		} finally {
			this.$lock.unlock();
		}
	}
}
class LockedPlainStatic {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private static final java.util.concurrent.locks.Lock $LOCK = new java.util.concurrent.locks.ReentrantLock();
	static void test() {
		LockedPlainStatic.$LOCK.lock();
		try {
			System.out.println("three");
		} finally {
			LockedPlainStatic.$LOCK.unlock();
		}
	}
	static void test2() {
		LockedPlainStatic.$LOCK.lock();
		try {
			System.out.println("four");
		} finally {
			LockedPlainStatic.$LOCK.unlock();
		}
	}
}
class LockedPlainRead {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private static final java.util.concurrent.locks.ReadWriteLock $LOCK = new java.util.concurrent.locks.ReentrantReadWriteLock();
	static void test() {
		LockedPlainRead.$LOCK.readLock().lock();
		try {
			System.out.println("five");
		} finally {
			LockedPlainRead.$LOCK.readLock().unlock();
		}
	}
	static void test2() {
		LockedPlainRead.$LOCK.readLock().lock();
		try {
			System.out.println("six");
		} finally {
			LockedPlainRead.$LOCK.readLock().unlock();
		}
	}
}
class LockedPlainWrite {
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private final java.util.concurrent.locks.ReadWriteLock $lock = new java.util.concurrent.locks.ReentrantReadWriteLock();
	void test() {
		this.$lock.writeLock().lock();
		try {
			System.out.println("seven");
		} finally {
			this.$lock.writeLock().unlock();
		}
	}
	void test2() {
		this.$lock.writeLock().lock();
		try {
			System.out.println("eight");
		} finally {
			this.$lock.writeLock().unlock();
		}
	}
}
