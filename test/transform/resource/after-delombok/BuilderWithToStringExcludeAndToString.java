public class BuilderWithToStringExcludeAndToString {
	private String a;
	private String secret;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderWithToStringExcludeAndToString(final String a, final String secret) {
		this.a = a;
		this.secret = secret;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderWithToStringExcludeAndToStringBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String a;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String secret;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderWithToStringExcludeAndToStringBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithToStringExcludeAndToString.BuilderWithToStringExcludeAndToStringBuilder a(final String a) {
			this.a = a;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithToStringExcludeAndToString.BuilderWithToStringExcludeAndToStringBuilder secret(final String secret) {
			this.secret = secret;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithToStringExcludeAndToString build() {
			return new BuilderWithToStringExcludeAndToString(this.a, this.secret);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderWithToStringExcludeAndToString.BuilderWithToStringExcludeAndToStringBuilder(a=" + this.a + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderWithToStringExcludeAndToString.BuilderWithToStringExcludeAndToStringBuilder builder() {
		return new BuilderWithToStringExcludeAndToString.BuilderWithToStringExcludeAndToStringBuilder();
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public java.lang.String toString() {
		return "BuilderWithToStringExcludeAndToString(a=" + this.a + ")";
	}
}
