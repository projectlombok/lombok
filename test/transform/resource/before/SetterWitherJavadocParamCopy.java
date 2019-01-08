class SetterWitherJavadocParamCopy {
	/**
	 * Some text
	 *
	 * @param fieldName Hello, World1
	 * --- GETTER ---
	 * Getter section
	 *
	 * @return Sky is blue1
	 */
	@lombok.Setter @lombok.experimental.Wither private int fieldName;
	
	public SetterWitherJavadocParamCopy(int f) {
		this.fieldName = f;
	}
}
