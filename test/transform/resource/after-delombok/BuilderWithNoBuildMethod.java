class BuilderWithNoBuildMethod {
	private String a;
	@java.lang.SuppressWarnings("all")
	BuilderWithNoBuildMethod(final String a) {
		this.a = a;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderWithNoBuildMethodBuilder {
		@java.lang.SuppressWarnings("all")
		private String a;
		@java.lang.SuppressWarnings("all")
		BuilderWithNoBuildMethodBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithNoBuildMethodBuilder a(final String a) {
			this.a = a;
			return this;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderWithNoBuildMethod.BuilderWithNoBuildMethodBuilder(a=" + this.a + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderWithNoBuildMethodBuilder builder() {
		return new BuilderWithNoBuildMethodBuilder();
	}
}
