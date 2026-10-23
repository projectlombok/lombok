class BuilderToBuilderMethodNameImplicit {
	private String name;
	private int age;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderToBuilderMethodNameImplicit(final String name, final int age) {
		this.name = name;
		this.age = age;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderToBuilderMethodNameImplicitBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String name;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int age;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderToBuilderMethodNameImplicitBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderToBuilderMethodNameImplicit.BuilderToBuilderMethodNameImplicitBuilder name(final String name) {
			this.name = name;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderToBuilderMethodNameImplicit.BuilderToBuilderMethodNameImplicitBuilder age(final int age) {
			this.age = age;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderToBuilderMethodNameImplicit build() {
			return new BuilderToBuilderMethodNameImplicit(this.name, this.age);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderToBuilderMethodNameImplicit.BuilderToBuilderMethodNameImplicitBuilder(name=" + this.name + ", age=" + this.age + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderToBuilderMethodNameImplicit.BuilderToBuilderMethodNameImplicitBuilder builder() {
		return new BuilderToBuilderMethodNameImplicit.BuilderToBuilderMethodNameImplicitBuilder();
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public BuilderToBuilderMethodNameImplicit.BuilderToBuilderMethodNameImplicitBuilder mutate() {
		return new BuilderToBuilderMethodNameImplicit.BuilderToBuilderMethodNameImplicitBuilder().name(this.name).age(this.age);
	}
}
