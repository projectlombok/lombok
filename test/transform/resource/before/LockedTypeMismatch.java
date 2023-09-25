class LockedGeneratedTypeMismatch {
	@lombok.Locked void test() {
		System.out.println("one");
	}
	@lombok.Locked.Read void test2() {
		System.out.println("two");
	}
}
class LockedUserTypeMismatch {
	private final java.util.concurrent.locks.Lock userLock = new java.util.concurrent.locks.ReentrantLock();
	@lombok.Locked("userLock") void test() {
		System.out.println("one");
	}
	@lombok.Locked.Read("userLock") void test2() {
		System.out.println("two");
	}
}
