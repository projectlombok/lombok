class BuilderToBuilderMethodNameConfig {
	private String name;
	private int age;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderToBuilderMethodNameConfig(final String name, final int age) {
		this.name = name;
		this.age = age;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderToBuilderMethodNameConfigBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String name;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int age;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderToBuilderMethodNameConfigBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderToBuilderMethodNameConfig.BuilderToBuilderMethodNameConfigBuilder name(final String name) {
			this.name = name;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderToBuilderMethodNameConfig.BuilderToBuilderMethodNameConfigBuilder age(final int age) {
			this.age = age;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderToBuilderMethodNameConfig build() {
			return new BuilderToBuilderMethodNameConfig(this.name, this.age);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderToBuilderMethodNameConfig.BuilderToBuilderMethodNameConfigBuilder(name=" + this.name + ", age=" + this.age + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderToBuilderMethodNameConfig.BuilderToBuilderMethodNameConfigBuilder builder() {
		return new BuilderToBuilderMethodNameConfig.BuilderToBuilderMethodNameConfigBuilder();
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public BuilderToBuilderMethodNameConfig.BuilderToBuilderMethodNameConfigBuilder mutate() {
		return new BuilderToBuilderMethodNameConfig.BuilderToBuilderMethodNameConfigBuilder().name(this.name).age(this.age);
	}
}
