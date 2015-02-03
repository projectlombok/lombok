final class UtilityClass {
	private static String someField;
	static void someMethod() {
		System.out.println();
	}
	protected static class InnerClass {
		private String innerInnerMember;
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	private UtilityClass() {
		throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
}
class UtilityInner {
	static class InnerInner {
		static final class InnerInnerInner {
			static int member;
			@java.lang.SuppressWarnings("all")
			@javax.annotation.Generated("lombok")
			private InnerInnerInner() {
				throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
			}
		}
	}
	enum UtilityInsideEnum {
		FOO,
		BAR;
		static final class InsideEnum {
			static int member;
			@java.lang.SuppressWarnings("all")
			@javax.annotation.Generated("lombok")
			private InsideEnum() {
				throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
			}
		}
	}
	interface UtilityInsideInterface {
		final class InsideInterface {
			static int member;
			@java.lang.SuppressWarnings("all")
			@javax.annotation.Generated("lombok")
			private InsideInterface() {
				throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
			}
		}
	}
}