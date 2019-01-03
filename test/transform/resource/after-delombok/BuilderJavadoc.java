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
	 * getsetwith gets a builder setter, an instance getter and setter, and a wither.
	 */
	private int getsetwith;
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
		private int getsetwith;
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
		 * getsetwith gets a builder setter, an instance getter and setter, and a wither.
		 * @param tag is moved to the setters and wither.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderJavadocBuilder<T> getsetwith(final int getsetwith) {
			this.getsetwith = getsetwith;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderJavadocBuilder<T> also(final List<T> also) {
			this.also = also;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderJavadoc<T> build() {
			return new BuilderJavadoc<T>(yes, getsetwith, predef, predefWithJavadoc, also);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderJavadoc.BuilderJavadocBuilder(yes=" + this.yes + ", getsetwith=" + this.getsetwith + ", predef=" + this.predef + ", predefWithJavadoc=" + this.predefWithJavadoc + ", also=" + this.also + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	BuilderJavadoc(final int yes, final int getsetwith, final int predef, final int predefWithJavadoc, final List<T> also) {
		this.yes = yes;
		this.getsetwith = getsetwith;
		this.predef = predef;
		this.predefWithJavadoc = predefWithJavadoc;
		this.also = also;
	}
	@java.lang.SuppressWarnings("all")
	public static <T> BuilderJavadocBuilder<T> builder() {
		return new BuilderJavadocBuilder<T>();
	}
	/**
	 * getsetwith gets a builder setter, an instance getter and setter, and a wither.
	 *
	 * @return tag is moved to the getter.
	 */
	@java.lang.SuppressWarnings("all")
	public int getGetsetwith() {
		return this.getsetwith;
	}
	/**
	 * getsetwith gets a builder setter, an instance getter and setter, and a wither.
	 * @param tag is moved to the setters and wither.
	 */
	@java.lang.SuppressWarnings("all")
	public void setGetsetwith(final int getsetwith) {
		this.getsetwith = getsetwith;
	}
	/**
	 * getsetwith gets a builder setter, an instance getter and setter, and a wither.
	 * @param tag is moved to the setters and wither.
	 */
	@java.lang.SuppressWarnings("all")
	public BuilderJavadoc<T> withGetsetwith(final int getsetwith) {
		return this.getsetwith == getsetwith ? this : new BuilderJavadoc<T>(this.yes, getsetwith, this.predef, this.predefWithJavadoc, this.also);
	}
}
