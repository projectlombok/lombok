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
	 */
	String withSections;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static final class Fields {
		/**
		 * Some text
		 */
		public static final java.lang.String withJavadoc = "withJavadoc";
		public static final java.lang.String withoutJavadoc = "withoutJavadoc";
		/**
		 * More text
		 */
		public static final java.lang.String withTags = "withTags";
		/**
		 * Main text
		 */
		public static final java.lang.String withSections = "withSections";
	}
	/**
	 * Getter section
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public String getWithSections() {
		return this.withSections;
	}
}
class FieldNameConstantsJavadoc2 {
	/**
	 * Enum text
	 */
	String withJavadoc;
	String withoutJavadoc;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public enum Fields {
		/**
		 * Enum text
		 */
		withJavadoc, withoutJavadoc;
	}
}
