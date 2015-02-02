@lombok.experimental.UtilityClass
class UtilityClass {
	private String someField;
	
	void someMethod() {
		System.out.println();
	}
	
	protected class InnerClass {
		private String innerInnerMember;
	}
}
class UtilityInner {
	static class InnerInner {
		@lombok.experimental.UtilityClass
		class InnerInnerInner {
			int member;
		}
	}
}
