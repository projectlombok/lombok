//version 8: Jackson deps are at least Java7+.
//CONF: lombok.builder.className = Test*Name
import java.util.List;
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder = JacksonizedBuilderComplex.TestVoidName.class)
class JacksonizedBuilderComplex {
	private static <T extends Number> void testVoidWithGenerics(T number, int arg2, String arg3, JacksonizedBuilderComplex selfRef) {
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	@com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix = "with", buildMethodName = "execute")
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
		private JacksonizedBuilderComplex selfRef;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		TestVoidName() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public JacksonizedBuilderComplex.TestVoidName<T> withNumber(final T number) {
			this.number = number;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public JacksonizedBuilderComplex.TestVoidName<T> withArg2(final int arg2) {
			this.arg2 = arg2;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public JacksonizedBuilderComplex.TestVoidName<T> withArg3(final String arg3) {
			this.arg3 = arg3;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public JacksonizedBuilderComplex.TestVoidName<T> withSelfRef(final JacksonizedBuilderComplex selfRef) {
			this.selfRef = selfRef;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public void execute() {
			JacksonizedBuilderComplex.<T>testVoidWithGenerics(this.number, this.arg2, this.arg3, this.selfRef);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "JacksonizedBuilderComplex.TestVoidName(number=" + this.number + ", arg2=" + this.arg2 + ", arg3=" + this.arg3 + ", selfRef=" + this.selfRef + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static <T extends Number> JacksonizedBuilderComplex.TestVoidName<T> builder() {
		return new JacksonizedBuilderComplex.TestVoidName<T>();
	}
}
