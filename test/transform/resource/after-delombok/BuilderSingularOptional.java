import java.util.Optional;
class BuilderSingularOptional<T> {
	private Optional<T> value;
	private Optional<String> name;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderSingularOptional(final Optional<T> value, final Optional<String> name) {
		this.value = value;
		this.name = name;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderSingularOptionalBuilder<T> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private T value$value;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String name$value;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderSingularOptionalBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderSingularOptional.BuilderSingularOptionalBuilder<T> value(final T value) {
			this.value$value = value;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderSingularOptional.BuilderSingularOptionalBuilder<T> clearValue() {
			this.value$value = null;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderSingularOptional.BuilderSingularOptionalBuilder<T> name(final String name) {
			this.name$value = name;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderSingularOptional.BuilderSingularOptionalBuilder<T> clearName() {
			this.name$value = null;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderSingularOptional<T> build() {
			java.util.Optional<T> value = java.util.Optional.ofNullable(this.value$value);
			java.util.Optional<String> name = java.util.Optional.ofNullable(this.name$value);
			return new BuilderSingularOptional<T>(value, name);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderSingularOptional.BuilderSingularOptionalBuilder(value$value=" + this.value$value + ", name$value=" + this.name$value + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static <T> BuilderSingularOptional.BuilderSingularOptionalBuilder<T> builder() {
		return new BuilderSingularOptional.BuilderSingularOptionalBuilder<T>();
	}
}
