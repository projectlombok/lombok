class BuilderWithNoBuilderMethod {
	private String a = "";
	@java.lang.SuppressWarnings("all")
	BuilderWithNoBuilderMethod(final String a) {
		this.a = a;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderWithNoBuilderMethodBuilder {
		@java.lang.SuppressWarnings("all")
		private String a;
		@java.lang.SuppressWarnings("all")
		BuilderWithNoBuilderMethodBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithNoBuilderMethod.BuilderWithNoBuilderMethodBuilder a(final String a) {
			this.a = a;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithNoBuilderMethod build() {
			return new BuilderWithNoBuilderMethod(this.a);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderWithNoBuilderMethod.BuilderWithNoBuilderMethodBuilder(a=" + this.a + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public BuilderWithNoBuilderMethod.BuilderWithNoBuilderMethodBuilder toBuilder() {
		return new BuilderWithNoBuilderMethod.BuilderWithNoBuilderMethodBuilder().a(this.a);
	}
}
