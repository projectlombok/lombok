public class BuilderNestedJavadoc {
	public static class NestedClass {
		/**
		 * Example javadoc
		 */
		String name;
		/**
		 * Creates a new {@code NestedClass} instance.
		 *
		 * @param name Example javadoc
		 */	
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		NestedClass(final String name) {
			this.name = name;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static class NestedClassBuilder {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private String name;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			NestedClassBuilder() {
			}
			/**
			 * Example javadoc
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public BuilderNestedJavadoc.NestedClass.NestedClassBuilder name(final String name) {
				this.name = name;
				return this;
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public BuilderNestedJavadoc.NestedClass build() {
				return new BuilderNestedJavadoc.NestedClass(this.name);
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public java.lang.String toString() {
				return "BuilderNestedJavadoc.NestedClass.NestedClassBuilder(name=" + this.name + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static BuilderNestedJavadoc.NestedClass.NestedClassBuilder builder() {
			return new BuilderNestedJavadoc.NestedClass.NestedClassBuilder();
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderNestedJavadoc() {
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderNestedJavadocBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderNestedJavadocBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderNestedJavadoc build() {
			return new BuilderNestedJavadoc();
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderNestedJavadoc.BuilderNestedJavadocBuilder()";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderNestedJavadoc.BuilderNestedJavadocBuilder builder() {
		return new BuilderNestedJavadoc.BuilderNestedJavadocBuilder();
	}
}
