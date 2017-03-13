final class UtilityClassErrors1 {
	private static String someField;
	protected UtilityClassErrors1() {
	}
	static void method() {
		class MethodLocalClass {
		}
	}
}
enum UtilityClassErrors2 {
	;
}
class UtilityClassErrors3 {
	class NonStaticInner {
		class ThisShouldFail {
			private String member;
		}
	}
}
