@lombok.Data
class GetterSetterJavadoc1 {
	/**
	 * Some text
	 *
	 * @param fieldName Hello, World
	 * --- GETTER ---
	 * Getter section
	 *
	 * @return Sky is blue
	 */
	private int fieldName;
}

class GetterSetterJavadoc2 {
	/**
	 * Some text
	 *
	 * @param fieldName Hello, World
	 * @return Sky is blue
	 */
	@lombok.Getter @lombok.Setter private int fieldName;
}

class GetterSetterJavadoc3 {
	/**
	 * Some text
	 *
	 * **SETTER**
	 * Setter section
	 * @param fieldName Hello, World
	 * **GETTER**
	 * Getter section
	 * @return Sky is blue
	 */
	@lombok.Getter @lombok.Setter private int fieldName;
}
