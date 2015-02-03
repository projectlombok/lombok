@lombok.experimental.UtilityClass
class UtilityClassErrors1 {
	private String someField;
	protected UtilityClassErrors1() {
	}
	void method() {
		@lombok.experimental.UtilityClass
		class MethodLocalClass {
		}
	}
}
@lombok.experimental.UtilityClass
enum UtilityClassErrors2 {
}
class UtilityClassErrors3 {
	class NonStaticInner {
		@lombok.experimental.UtilityClass
		class ThisShouldFail {
			private String member;
		}
	}
}