final class UtilityClass {
	private static long someField = System.currentTimeMillis();
	static void someMethod() {
		System.out.println();
		new InnerClass();
		new InnerStaticClass();
	}
	protected static class InnerClass {
		private String innerInnerMember;
	}
	protected static class InnerStaticClass {
		private String innerInnerMember;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private UtilityClass() {
		throw new java.lang.AssertionError("This is a utility class and cannot be instantiated");
	}
}
class UtilityInner {
	static class InnerInner {
		static final class InnerInnerInner {
			static int member;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private InnerInnerInner() {
				throw new java.lang.AssertionError("This is a utility class and cannot be instantiated");
			}
		}
	}
	enum UtilityInsideEnum {
		FOO, BAR;
		static final class InsideEnum {
			static int member;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private InsideEnum() {
				throw new java.lang.AssertionError("This is a utility class and cannot be instantiated");
			}
		}
	}
	interface UtilityInsideInterface {
		final class InsideInterface {
			static int member;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private InsideInterface() {
				throw new java.lang.AssertionError("This is a utility class and cannot be instantiated");
			}
		}
	}
}
