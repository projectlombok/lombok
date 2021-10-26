class BuilderNestedInEnum {
	public enum TestEnum {
		FOO, BAR;
		public static final class TestBuilder {
			private final String field;
			@java.lang.SuppressWarnings("all")
			TestBuilder(final String field) {
				this.field = field;
			}
			@java.lang.SuppressWarnings("all")
			public static class TestBuilderBuilder {
				@java.lang.SuppressWarnings("all")
				private String field;
				@java.lang.SuppressWarnings("all")
				TestBuilderBuilder() {
				}
				/**
				 * @return {@code this}.
				 */
				@java.lang.SuppressWarnings("all")
				public BuilderNestedInEnum.TestEnum.TestBuilder.TestBuilderBuilder field(final String field) {
					this.field = field;
					return this;
				}
				@java.lang.SuppressWarnings("all")
				public BuilderNestedInEnum.TestEnum.TestBuilder build() {
					return new BuilderNestedInEnum.TestEnum.TestBuilder(this.field);
				}
				@java.lang.Override
				@java.lang.SuppressWarnings("all")
				public java.lang.String toString() {
					return "BuilderNestedInEnum.TestEnum.TestBuilder.TestBuilderBuilder(field=" + this.field + ")";
				}
			}
			@java.lang.SuppressWarnings("all")
			public static BuilderNestedInEnum.TestEnum.TestBuilder.TestBuilderBuilder builder() {
				return new BuilderNestedInEnum.TestEnum.TestBuilder.TestBuilderBuilder();
			}
			@java.lang.SuppressWarnings("all")
			public String getField() {
				return this.field;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public boolean equals(final java.lang.Object o) {
				if (o == this) return true;
				if (!(o instanceof BuilderNestedInEnum.TestEnum.TestBuilder)) return false;
				final BuilderNestedInEnum.TestEnum.TestBuilder other = (BuilderNestedInEnum.TestEnum.TestBuilder) o;
				final java.lang.Object this$field = this.getField();
				final java.lang.Object other$field = other.getField();
				if (this$field == null ? other$field != null : !this$field.equals(other$field)) return false;
				return true;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public int hashCode() {
				final int PRIME = 59;
				int result = 1;
				final java.lang.Object $field = this.getField();
				result = result * PRIME + ($field == null ? 43 : $field.hashCode());
				return result;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "BuilderNestedInEnum.TestEnum.TestBuilder(field=" + this.getField() + ")";
			}
		}
	}
}