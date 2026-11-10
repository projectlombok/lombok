public class BuilderWithToStringExcludeOld {
	private String a;
	private String secret;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderWithToStringExcludeOld(final String a, final String secret) {
		this.a = a;
		this.secret = secret;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderWithToStringExcludeOldBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String a;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String secret;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderWithToStringExcludeOldBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithToStringExcludeOld.BuilderWithToStringExcludeOldBuilder a(final String a) {
			this.a = a;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithToStringExcludeOld.BuilderWithToStringExcludeOldBuilder secret(final String secret) {
			this.secret = secret;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithToStringExcludeOld build() {
			return new BuilderWithToStringExcludeOld(this.a, this.secret);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderWithToStringExcludeOld.BuilderWithToStringExcludeOldBuilder(a=" + this.a + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderWithToStringExcludeOld.BuilderWithToStringExcludeOldBuilder builder() {
		return new BuilderWithToStringExcludeOld.BuilderWithToStringExcludeOldBuilder();
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public java.lang.String toString() {
		return "BuilderWithToStringExcludeOld(a=" + this.a + ")";
	}
}
