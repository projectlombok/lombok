import java.util.List;
class BuilderJavadoc<T> {
	/**
	 * basic gets only a builder setter.
	 * @see #getsetwith
	 * @return tag is removed from the setter.
	 */
	private final int basic;
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
	public static class BuilderJavadocBuilder<T> {
		@java.lang.SuppressWarnings("all")
		private int basic;
		@java.lang.SuppressWarnings("all")
		private int getsetwith;
		@java.lang.SuppressWarnings("all")
		private int predef;
		@java.lang.SuppressWarnings("all")
		private int predefWithJavadoc;
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
		 * basic gets only a builder setter.
		 * @see #getsetwith
		 * @param tag is moved to the setter.
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderJavadoc.BuilderJavadocBuilder<T> basic(final int basic) {
			this.basic = basic;
			return this;
		}
		/**
		 * getsetwith gets a builder setter, an instance getter and setter, and a wither.
		 * @param tag is moved to the setters and wither.
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderJavadoc.BuilderJavadocBuilder<T> getsetwith(final int getsetwith) {
			this.getsetwith = getsetwith;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderJavadoc<T> build() {
			return new BuilderJavadoc<T>(this.basic, this.getsetwith, this.predef, this.predefWithJavadoc);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderJavadoc.BuilderJavadocBuilder(basic=" + this.basic + ", getsetwith=" + this.getsetwith + ", predef=" + this.predef + ", predefWithJavadoc=" + this.predefWithJavadoc + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	BuilderJavadoc(final int basic, final int getsetwith, final int predef, final int predefWithJavadoc) {
		this.basic = basic;
		this.getsetwith = getsetwith;
		this.predef = predef;
		this.predefWithJavadoc = predefWithJavadoc;
	}
	@java.lang.SuppressWarnings("all")
	public static <T> BuilderJavadoc.BuilderJavadocBuilder<T> builder() {
		return new BuilderJavadoc.BuilderJavadocBuilder<T>();
	}
	/**
	 * getsetwith gets a builder setter, an instance getter and setter, and a wither.
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
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	public BuilderJavadoc<T> withGetsetwith(final int getsetwith) {
		return this.getsetwith == getsetwith ? this : new BuilderJavadoc<T>(this.basic, getsetwith, this.predef, this.predefWithJavadoc);
	}
}
