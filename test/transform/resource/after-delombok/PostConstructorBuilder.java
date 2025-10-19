class PostConstructorBuilder {
	private String field;

	private void postConstructor() {
	}

	@java.lang.SuppressWarnings("all")
	PostConstructorBuilder(final String field) {
		this.field = field;
		postConstructor();
	}


	@java.lang.SuppressWarnings("all")
	public static class PostConstructorBuilderBuilder {
		@java.lang.SuppressWarnings("all")
		private String field;

		@java.lang.SuppressWarnings("all")
		PostConstructorBuilderBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public PostConstructorBuilder.PostConstructorBuilderBuilder field(final String field) {
			this.field = field;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public PostConstructorBuilder build() {
			return new PostConstructorBuilder(this.field);
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "PostConstructorBuilder.PostConstructorBuilderBuilder(field=" + this.field + ")";
		}
	}

	@java.lang.SuppressWarnings("all")
	public static PostConstructorBuilder.PostConstructorBuilderBuilder builder() {
		return new PostConstructorBuilder.PostConstructorBuilderBuilder();
	}
}
