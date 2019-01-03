import java.util.List;
class BuilderJavadoc<T> {
	/**
	 * Will not get a setter on the builder.
	 */
	private final int noshow = 0;
	/**
	 * Yes, yes gets a setter.
	 * @see #also
	 *
	 * @return tag is removed from the setter.
	 */
	private final int yes;
	private List<T> also;
	/**
	 * But this one doesn't.
	 */
	private int $butNotMe;
	@java.lang.SuppressWarnings("all")
	BuilderJavadoc(final int yes, final List<T> also) {
		this.yes = yes;
		this.also = also;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderJavadocBuilder<T> {
		@java.lang.SuppressWarnings("all")
		private int yes;
		@java.lang.SuppressWarnings("all")
		private List<T> also;
		@java.lang.SuppressWarnings("all")
		BuilderJavadocBuilder() {
		}
		/**
		 * Yes, yes gets a setter.
		 * @see #also
		 * @param tag is moved to the setter.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderJavadocBuilder<T> yes(final int yes) {
			this.yes = yes;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderJavadocBuilder<T> also(final List<T> also) {
			this.also = also;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderJavadoc<T> build() {
			return new BuilderJavadoc<T>(yes, also);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderJavadoc.BuilderJavadocBuilder(yes=" + this.yes + ", also=" + this.also + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static <T> BuilderJavadocBuilder<T> builder() {
		return new BuilderJavadocBuilder<T>();
	}
}
