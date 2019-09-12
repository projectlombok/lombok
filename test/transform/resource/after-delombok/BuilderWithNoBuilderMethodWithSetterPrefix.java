class BuilderWithNoBuilderMethodWithSetterPrefix {
	private String a = "";
	@java.lang.SuppressWarnings("all")
	BuilderWithNoBuilderMethodWithSetterPrefix(final String a) {
		this.a = a;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderWithNoBuilderMethodWithSetterPrefixBuilder {
		@java.lang.SuppressWarnings("all")
		private String a;
		@java.lang.SuppressWarnings("all")
		BuilderWithNoBuilderMethodWithSetterPrefixBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithNoBuilderMethodWithSetterPrefixBuilder withA(final String a) {
			this.a = a;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithNoBuilderMethodWithSetterPrefix build() {
			return new BuilderWithNoBuilderMethodWithSetterPrefix(a);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderWithNoBuilderMethodWithSetterPrefix.BuilderWithNoBuilderMethodWithSetterPrefixBuilder(a=" + this.a + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public BuilderWithNoBuilderMethodWithSetterPrefixBuilder toBuilder() {
		return new BuilderWithNoBuilderMethodWithSetterPrefixBuilder().withA(this.a);
	}
}
