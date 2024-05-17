import java.util.List;
class BuilderComplex {
	private static <T extends Number> void testVoidWithGenerics(T number, int arg2, String arg3, BuilderComplex selfRef) {
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class TestVoidName<T extends Number> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private T number;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int arg2;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String arg3;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private BuilderComplex selfRef;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		TestVoidName() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderComplex.TestVoidName<T> number(final T number) {
			this.number = number;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderComplex.TestVoidName<T> arg2(final int arg2) {
			this.arg2 = arg2;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderComplex.TestVoidName<T> arg3(final String arg3) {
			this.arg3 = arg3;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderComplex.TestVoidName<T> selfRef(final BuilderComplex selfRef) {
			this.selfRef = selfRef;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public void execute() {
			BuilderComplex.<T>testVoidWithGenerics(this.number, this.arg2, this.arg3, this.selfRef);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderComplex.TestVoidName(number=" + this.number + ", arg2=" + this.arg2 + ", arg3=" + this.arg3 + ", selfRef=" + this.selfRef + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static <T extends Number> BuilderComplex.TestVoidName<T> builder() {
		return new BuilderComplex.TestVoidName<T>();
	}
}
