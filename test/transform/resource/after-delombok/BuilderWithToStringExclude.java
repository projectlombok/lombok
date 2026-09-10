public class BuilderWithToStringExclude {
	private String a;
	private String secret;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderWithToStringExclude(final String a, final String secret) {
		this.a = a;
		this.secret = secret;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderWithToStringExcludeBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String a;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String secret;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderWithToStringExcludeBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithToStringExclude.BuilderWithToStringExcludeBuilder a(final String a) {
			this.a = a;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithToStringExclude.BuilderWithToStringExcludeBuilder secret(final String secret) {
			this.secret = secret;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithToStringExclude build() {
			return new BuilderWithToStringExclude(this.a, this.secret);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderWithToStringExclude.BuilderWithToStringExcludeBuilder(a=" + this.a + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderWithToStringExclude.BuilderWithToStringExcludeBuilder builder() {
		return new BuilderWithToStringExclude.BuilderWithToStringExcludeBuilder();
	}
}
