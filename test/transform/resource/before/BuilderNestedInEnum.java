// issue #3014: Builder check if its on a non-static inner class and errors if it is. But it was erroring here even though it is on a static inner class.
class BuilderNestedInEnum {
	public enum TestEnum {
		FOO, BAR;
		
		@lombok.Builder
		@lombok.Value
		public static class TestBuilder {
			String field;
		}
	}
}