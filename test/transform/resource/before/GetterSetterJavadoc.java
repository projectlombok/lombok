@lombok.Data
class GetterSetterJavadoc1 {
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
}

class GetterSetterJavadoc2 {
	/**
	 * Some text
	 *
	 * @param fieldName Hello, World2
	 * @return Sky is blue2
	 */
	@lombok.Getter @lombok.Setter private int fieldName;
}

class GetterSetterJavadoc3 {
	/**
	 * Some text
	 *
	 * **SETTER**
	 * Setter section
	 * @param fieldName Hello, World3
	 * **GETTER**
	 * Getter section
	 * @return Sky is blue3
	 */
	@lombok.Getter @lombok.Setter private int fieldName;
}
