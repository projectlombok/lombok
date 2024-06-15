public class ConstructorJavadoc {
	/**
	 * Some text
	 *
	 * @param fieldName Hello, World1
	 * --- GETTER ---
	 * Getter section
	 *
	 * @return Sky is blue1
	 */
	private int fieldName;
	/**
	 * Sky is blue
	 */
	private int fieldName2;
	/**
	 * Creates a new {@code ConstructorJavadoc} instance.
	 *
	 * @param fieldName Hello, World1
	 * @param fieldName2 Sky is blue
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public ConstructorJavadoc(final int fieldName, final int fieldName2) {
		this.fieldName = fieldName;
		this.fieldName2 = fieldName2;
	}
}
