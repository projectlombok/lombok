//version 8:
class BuilderWithNonNull {
	@lombok.NonNull
	private final String id;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderWithNonNull(@lombok.NonNull final String id) {
		if (id == null) {
			throw new java.lang.NullPointerException("id is marked non-null but is null");
		}
		this.id = id;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderWithNonNullBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String id;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderWithNonNullBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithNonNull.BuilderWithNonNullBuilder id(@lombok.NonNull final String id) {
			if (id == null) {
				throw new java.lang.NullPointerException("id is marked non-null but is null");
			}
			this.id = id;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithNonNull build() {
			return new BuilderWithNonNull(this.id);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderWithNonNull.BuilderWithNonNullBuilder(id=" + this.id + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderWithNonNull.BuilderWithNonNullBuilder builder() {
		return new BuilderWithNonNull.BuilderWithNonNullBuilder();
	}
}
