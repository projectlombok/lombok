class LockedGeneratedTypeMismatch {
	@lombok.experimental.Locked void test() {
		System.out.println("one");
	}
	@lombok.experimental.Locked.Read void test2() {
		System.out.println("two");
	}
}
class LockedUserTypeMismatch {
	private final java.util.concurrent.locks.ReentrantLock userLock = new java.util.concurrent.locks.ReentrantLock();
	@lombok.experimental.Locked("userLock") void test() {
		System.out.println("one");
	}
	@lombok.experimental.Locked.Read("userLock") void test2() {
		System.out.println("two");
	}
}
