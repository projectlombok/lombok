//version 8:
class BuilderWithNonNullWithSetterPrefix {
	@lombok.NonNull
	private final String id;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderWithNonNullWithSetterPrefix(@lombok.NonNull final String id) {
		if (id == null) {
			throw new java.lang.NullPointerException("id is marked non-null but is null");
		}
		this.id = id;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderWithNonNullWithSetterPrefixBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String id;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderWithNonNullWithSetterPrefixBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithNonNullWithSetterPrefix.BuilderWithNonNullWithSetterPrefixBuilder withId(@lombok.NonNull final String id) {
			if (id == null) {
				throw new java.lang.NullPointerException("id is marked non-null but is null");
			}
			this.id = id;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithNonNullWithSetterPrefix build() {
			return new BuilderWithNonNullWithSetterPrefix(this.id);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderWithNonNullWithSetterPrefix.BuilderWithNonNullWithSetterPrefixBuilder(id=" + this.id + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderWithNonNullWithSetterPrefix.BuilderWithNonNullWithSetterPrefixBuilder builder() {
		return new BuilderWithNonNullWithSetterPrefix.BuilderWithNonNullWithSetterPrefixBuilder();
	}
}
