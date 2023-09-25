import java.util.concurrent.locks.*;
class LockedName {
	private final Lock basicLock = new ReentrantLock();
	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	@lombok.Locked("basicLock") void test() {
		System.out.println("one");
	}
	@lombok.Locked.Read("rwLock") void test2() {
		System.out.println("two");
	}
	@lombok.Locked.Write("rwLock") void test3() {
		System.out.println("three");
	}
}
