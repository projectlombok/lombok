import java.util.List;

@lombok.Builder
class BuilderJavadoc<T> {
	/**
	 * Will not get a setter on the builder.
	 */
	private final int noshow = 0;

	/**
	 * Yes, yes gets a setter.
     * @see #also
	 * @param tag is moved to the setter.
	 * @return tag is removed from the setter and from the field.
	 */
	private final int yes;

	/**
	 * getsetwith gets a builder setter, an instance getter and setter, and a wither.
	 * @param tag is moved to the setters and wither.
	 * @return tag is moved to the getter.
	 */
	@lombok.Getter
	@lombok.Setter
	@lombok.experimental.Wither
	private int getsetwith;

	/**
	 * Predef has a predefined builder setter with no javadoc, and the builder setter does not get this one.
	 * @param tag is removed from the field.
	 * @return tag is removed from the field.
	 */
	private final int predef;

	/**
	 * predefWithJavadoc has a predefined builder setter with javadoc, so it keeps that one untouched.
	 * @param tag is removed from the field.
	 * @return tag is removed from the field.
	 */
	private final int predefWithJavadoc;

	private List<T> also;

	/**
	 * But this one doesn't.
	 */
	private int $butNotMe;

	public static class BuilderJavadocBuilder<T> {
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
    }

}
