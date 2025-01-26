public class BuilderWithBadNames {
	String build;
	String toString;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderWithBadNames(final String build, final String toString) {
		this.build = build;
		this.toString = toString;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderWithBadNamesBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String build;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String toString;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderWithBadNamesBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithBadNames.BuilderWithBadNamesBuilder build(final String build) {
			this.build = build;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithBadNames.BuilderWithBadNamesBuilder toString(final String toString) {
			this.toString = toString;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithBadNames build() {
			return new BuilderWithBadNames(this.build, this.toString);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderWithBadNames.BuilderWithBadNamesBuilder(build=" + this.build + ", toString=" + this.toString + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderWithBadNames.BuilderWithBadNamesBuilder builder() {
		return new BuilderWithBadNames.BuilderWithBadNamesBuilder();
	}
}
