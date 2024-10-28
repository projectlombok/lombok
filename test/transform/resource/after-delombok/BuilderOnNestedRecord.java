// version 14:
public record BuilderOnNestedRecord<T>(T t) {
	public record Nested(String a) {
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
			public BuilderOnNestedRecord.Nested.NestedBuilder a(final String a) {
				this.a = a;
				return this;
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public BuilderOnNestedRecord.Nested build() {
				return new BuilderOnNestedRecord.Nested(this.a);
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public java.lang.String toString() {
				return "BuilderOnNestedRecord.Nested.NestedBuilder(a=" + this.a + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static BuilderOnNestedRecord.Nested.NestedBuilder builder() {
			return new BuilderOnNestedRecord.Nested.NestedBuilder();
		}
	}
}
