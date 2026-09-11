class BuilderToBuilderMethodName {
	private String name;
	private int age;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderToBuilderMethodName(final String name, final int age) {
		this.name = name;
		this.age = age;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderToBuilderMethodNameBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String name;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int age;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderToBuilderMethodNameBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderToBuilderMethodName.BuilderToBuilderMethodNameBuilder name(final String name) {
			this.name = name;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderToBuilderMethodName.BuilderToBuilderMethodNameBuilder age(final int age) {
			this.age = age;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderToBuilderMethodName build() {
			return new BuilderToBuilderMethodName(this.name, this.age);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderToBuilderMethodName.BuilderToBuilderMethodNameBuilder(name=" + this.name + ", age=" + this.age + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderToBuilderMethodName.BuilderToBuilderMethodNameBuilder builder() {
		return new BuilderToBuilderMethodName.BuilderToBuilderMethodNameBuilder();
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public BuilderToBuilderMethodName.BuilderToBuilderMethodNameBuilder mutate() {
		return new BuilderToBuilderMethodName.BuilderToBuilderMethodNameBuilder().name(this.name).age(this.age);
	}
}
