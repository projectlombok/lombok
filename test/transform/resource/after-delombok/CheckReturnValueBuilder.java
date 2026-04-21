class CheckReturnValueBuilder {
	private final int x;
	private final String name;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	CheckReturnValueBuilder(final int x, final String name) {
		this.x = x;
		this.name = name;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class CheckReturnValueBuilderBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int x;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String name;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		CheckReturnValueBuilderBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public CheckReturnValueBuilder.CheckReturnValueBuilderBuilder x(final int x) {
			this.x = x;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public CheckReturnValueBuilder.CheckReturnValueBuilderBuilder name(final String name) {
			this.name = name;
			return this;
		}
		@lombok.CheckReturnValue
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public CheckReturnValueBuilder build() {
			return new CheckReturnValueBuilder(this.x, this.name);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "CheckReturnValueBuilder.CheckReturnValueBuilderBuilder(x=" + this.x + ", name=" + this.name + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static CheckReturnValueBuilder.CheckReturnValueBuilderBuilder builder() {
		return new CheckReturnValueBuilder.CheckReturnValueBuilderBuilder();
	}
}
