import lombok.Locked;
class LockedPlain {
	@Locked void test() {
		System.out.println("one");
	}
	@Locked void test2() {
		System.out.println("two");
	}
}
class LockedPlainStatic {
	@Locked static void test() {
		System.out.println("three");
	}
	@Locked static void test2() {
		System.out.println("four");
	}
}
class LockedPlainRead {
	@Locked.Read static void test() {
		System.out.println("five");
	}
	@Locked.Read static void test2() {
		System.out.println("six");
	}
}
class LockedPlainWrite {
	@Locked.Write void test() {
		System.out.println("seven");
	}
	@Locked.Write void test2() {
		System.out.println("eight");
	}
}
