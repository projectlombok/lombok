import java.util.List;
class BuilderComplex {
	private static <T extends Number> void testVoidWithGenerics(T number, int arg2, String arg3, BuilderComplex selfRef) {
	}
	@java.lang.SuppressWarnings("all")
	public static class TestVoidName<T extends Number> {
		@java.lang.SuppressWarnings("all")
		private T number;
		@java.lang.SuppressWarnings("all")
		private int arg2;
		@java.lang.SuppressWarnings("all")
		private String arg3;
		@java.lang.SuppressWarnings("all")
		private BuilderComplex selfRef;
		@java.lang.SuppressWarnings("all")
		TestVoidName() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderComplex.TestVoidName<T> number(final T number) {
			this.number = number;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderComplex.TestVoidName<T> arg2(final int arg2) {
			this.arg2 = arg2;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderComplex.TestVoidName<T> arg3(final String arg3) {
			this.arg3 = arg3;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderComplex.TestVoidName<T> selfRef(final BuilderComplex selfRef) {
			this.selfRef = selfRef;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public void execute() {
			BuilderComplex.<T>testVoidWithGenerics(this.number, this.arg2, this.arg3, this.selfRef);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderComplex.TestVoidName(number=" + this.number + ", arg2=" + this.arg2 + ", arg3=" + this.arg3 + ", selfRef=" + this.selfRef + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static <T extends Number> BuilderComplex.TestVoidName<T> builder() {
		return new BuilderComplex.TestVoidName<T>();
	}
}
