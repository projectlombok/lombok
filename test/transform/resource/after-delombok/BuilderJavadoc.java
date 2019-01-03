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
	/**
	 * getset gets a builder setter and an instance getter and setter.
	 */
	private int getset;
	/**
	 * Predef has a predefined builder setter with no javadoc, and the builder setter does not get this one.
	 * @param tag remains on the field.
	 * @return tag remains on the field.
	 */
	private final int predef;
	/**
	 * predefWithJavadoc has a predefined builder setter with javadoc, so it keeps that one untouched.
	 * @param tag is removed from the field.
	 * @return tag remains on the field.
	 */
	private final int predefWithJavadoc;
	private List<T> also;
	/**
	 * But this one doesn't.
	 */
	private int $butNotMe;
	public static class BuilderJavadocBuilder<T> {
		@java.lang.SuppressWarnings("all")
		private int yes;
		@java.lang.SuppressWarnings("all")
		private int getset;
		@java.lang.SuppressWarnings("all")
		private int predef;
		@java.lang.SuppressWarnings("all")
		private int predefWithJavadoc;
		@java.lang.SuppressWarnings("all")
		private List<T> also;
		public BuilderJavadocBuilder<T> predef(final int x) {
			this.predef = x * 10;
			return this;
		}
		/**
		 * This javadoc remains untouched.
		 * @param x 1/100 of the thing
		 * @return the updated builder
		 */
		public BuilderJavadocBuilder<T> predefWithJavadoc(final int x) {
			this.predefWithJavadoc = x * 100;
			return this;
		}
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
		/**
		 * getset gets a builder setter and an instance getter and setter.
		 * @param tag is moved to the setters.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderJavadocBuilder<T> getset(final int getset) {
			this.getset = getset;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderJavadocBuilder<T> also(final List<T> also) {
			this.also = also;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderJavadoc<T> build() {
			return new BuilderJavadoc<T>(yes, getset, predef, predefWithJavadoc, also);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderJavadoc.BuilderJavadocBuilder(yes=" + this.yes + ", getset=" + this.getset + ", predef=" + this.predef + ", predefWithJavadoc=" + this.predefWithJavadoc + ", also=" + this.also + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	BuilderJavadoc(final int yes, final int getset, final int predef, final int predefWithJavadoc, final List<T> also) {
		this.yes = yes;
		this.getset = getset;
		this.predef = predef;
		this.predefWithJavadoc = predefWithJavadoc;
		this.also = also;
	}
	@java.lang.SuppressWarnings("all")
	public static <T> BuilderJavadocBuilder<T> builder() {
		return new BuilderJavadocBuilder<T>();
	}
	/**
	 * getset gets a builder setter and an instance getter and setter.
	 *
	 * @return tag is moved to the getter.
	 */
	@java.lang.SuppressWarnings("all")
	public int getGetset() {
		return this.getset;
	}
	/**
	 * getset gets a builder setter and an instance getter and setter.
	 * @param tag is moved to the setters.
	 */
	@java.lang.SuppressWarnings("all")
	public void setGetset(final int getset) {
		this.getset = getset;
	}
}
