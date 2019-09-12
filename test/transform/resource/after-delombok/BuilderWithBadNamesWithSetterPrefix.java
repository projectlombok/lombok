public class BuilderWithBadNamesWithSetterPrefix {
	String build;
	String toString;
	@java.lang.SuppressWarnings("all")
	BuilderWithBadNamesWithSetterPrefix(final String build, final String toString) {
		this.build = build;
		this.toString = toString;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderWithBadNamesWithSetterPrefixBuilder {
		@java.lang.SuppressWarnings("all")
		private String build;
		@java.lang.SuppressWarnings("all")
		private String toString;
		@java.lang.SuppressWarnings("all")
		BuilderWithBadNamesWithSetterPrefixBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithBadNamesWithSetterPrefixBuilder withBuild(final String build) {
			this.build = build;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithBadNamesWithSetterPrefixBuilder withToString(final String toString) {
			this.toString = toString;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithBadNamesWithSetterPrefix build() {
			return new BuilderWithBadNamesWithSetterPrefix(build, toString);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderWithBadNamesWithSetterPrefix.BuilderWithBadNamesWithSetterPrefixBuilder(build=" + this.build + ", toString=" + this.toString + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderWithBadNamesWithSetterPrefixBuilder builder() {
		return new BuilderWithBadNamesWithSetterPrefixBuilder();
	}
}
