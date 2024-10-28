class BuilderWithNoBuilderMethod {
	private String a = "";
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderWithNoBuilderMethod(final String a) {
		this.a = a;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderWithNoBuilderMethodBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String a;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderWithNoBuilderMethodBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithNoBuilderMethod.BuilderWithNoBuilderMethodBuilder a(final String a) {
			this.a = a;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithNoBuilderMethod build() {
			return new BuilderWithNoBuilderMethod(this.a);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderWithNoBuilderMethod.BuilderWithNoBuilderMethodBuilder(a=" + this.a + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public BuilderWithNoBuilderMethod.BuilderWithNoBuilderMethodBuilder toBuilder() {
		return new BuilderWithNoBuilderMethod.BuilderWithNoBuilderMethodBuilder().a(this.a);
	}
}
