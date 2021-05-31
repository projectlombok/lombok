class LockedName {
	@lombok.experimental.Locked("basicLock") void test() {
		System.out.println("one");
	}
	@lombok.experimental.Locked.Read("rwLock") void test2() {
		System.out.println("two");
	}
	@lombok.experimental.Locked.Write("rwLock") void test3() {
		System.out.println("three");
	}
}
