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
	 * @return tag is removed from the setter.
	 */
	private final int yes;

	private List<T> also;

	/**
	 * But this one doesn't.
	 */
	private int $butNotMe;
}
