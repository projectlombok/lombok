public class BuilderOnNestedClass<T> {
	private T t;
	public static class Nested {
		private String a;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		Nested(final String a) {
			this.a = a;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static class NestedBuilder {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private String a;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			NestedBuilder() {
			}
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public BuilderOnNestedClass.Nested.NestedBuilder a(final String a) {
				this.a = a;
				return this;
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public BuilderOnNestedClass.Nested build() {
				return new BuilderOnNestedClass.Nested(this.a);
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public java.lang.String toString() {
				return "BuilderOnNestedClass.Nested.NestedBuilder(a=" + this.a + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static BuilderOnNestedClass.Nested.NestedBuilder builder() {
			return new BuilderOnNestedClass.Nested.NestedBuilder();
		}
	}
}
