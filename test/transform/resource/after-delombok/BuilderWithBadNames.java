public class BuilderWithBadNames {
	String build;
	String toString;
	@java.lang.SuppressWarnings("all")
	BuilderWithBadNames(final String build, final String toString) {
		this.build = build;
		this.toString = toString;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderWithBadNamesBuilder {
		@java.lang.SuppressWarnings("all")
		private String build;
		@java.lang.SuppressWarnings("all")
		private String toString;
		@java.lang.SuppressWarnings("all")
		BuilderWithBadNamesBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithBadNamesBuilder build(final String build) {
			this.build = build;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithBadNamesBuilder toString(final String toString) {
			this.toString = toString;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithBadNames build() {
			return new BuilderWithBadNames(build, toString);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderWithBadNames.BuilderWithBadNamesBuilder(build=" + this.build + ", toString=" + this.toString + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderWithBadNamesBuilder builder() {
		return new BuilderWithBadNamesBuilder();
	}
}
