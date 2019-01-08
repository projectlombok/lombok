class SetterWitherJavadocParamCopy {
	/**
	 * Some text
	 */
	private int fieldName;

	public SetterWitherJavadocParamCopy(int f) {
		this.fieldName = f;
	}
	/**
	 * Some text
	 *
	 * @param fieldName Hello, World1
	 */
	@java.lang.SuppressWarnings("all")
	public void setFieldName(final int fieldName) {
		this.fieldName = fieldName;
	}
	/**
	 * Some text
	 *
	 * @param fieldName Hello, World1
	 */
	@java.lang.SuppressWarnings("all")
	public SetterWitherJavadocParamCopy withFieldName(final int fieldName) {
		return this.fieldName == fieldName ? this : new SetterWitherJavadocParamCopy(fieldName);
	}
}