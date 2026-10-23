import lombok.experimental.FieldNameConstants;

@FieldNameConstants
class FieldNameConstantsJavadoc1 {
	/**
	 * Some text
	 */
	String withJavadoc;
	String withoutJavadoc;
	/**
	 * More text
	 *
	 * @param withTags Hello, World
	 * @return Sky is blue
	 */
	String withTags;
	/**
	 * Main text
	 *
	 * --- GETTER ---
	 * Getter section
	 */
	@lombok.Getter String withSections;
}

@FieldNameConstants(asEnum = true)
class FieldNameConstantsJavadoc2 {
	/**
	 * Enum text
	 */
	String withJavadoc;
	String withoutJavadoc;
}
