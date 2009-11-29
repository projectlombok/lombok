import lombok.Synchronized;
class SynchronizedPlain1 {
	@lombok.Synchonized void test() {
		System.out.println("one");
	}
	@Synchonized void test2() {
		System.out.println("two");
	}
}
class SynchronizedPlain2 {
	@lombok.Synchonized static void test() {
		System.out.println("three");
	}
	@Synchonized static void test2() {
		System.out.println("four");
	}
}