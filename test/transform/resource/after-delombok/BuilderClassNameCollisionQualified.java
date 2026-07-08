class BuilderClassNameCollisionQualified<T> {
	private java.util.function.Function<T, String> mapper;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderClassNameCollisionQualified(final java.util.function.Function<T, String> mapper) {
		this.mapper = mapper;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class Builder<T> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private java.util.function.Function<T, String> mapper;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		Builder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderClassNameCollisionQualified.Builder<T> mapper(final java.util.function.Function<T, String> mapper) {
			this.mapper = mapper;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderClassNameCollisionQualified<T> build() {
			return new BuilderClassNameCollisionQualified<T>(this.mapper);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderClassNameCollisionQualified.Builder(mapper=" + this.mapper + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static <T> BuilderClassNameCollisionQualified.Builder<T> builder() {
		return new BuilderClassNameCollisionQualified.Builder<T>();
	}
}
