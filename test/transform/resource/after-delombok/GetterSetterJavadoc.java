class GetterSetterJavadoc1 {
	/**
	 * Some text
	 */
	private int fieldName;
	@java.lang.SuppressWarnings("all")
	public GetterSetterJavadoc1() {
	}
	/**
	 * Getter section
	 *
	 * @return Sky is blue1
	 */
	@java.lang.SuppressWarnings("all")
	public int getFieldName() {
		return this.fieldName;
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
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof GetterSetterJavadoc1)) return false;
		final GetterSetterJavadoc1 other = (GetterSetterJavadoc1) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (this.getFieldName() != other.getFieldName()) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof GetterSetterJavadoc1;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getFieldName();
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "GetterSetterJavadoc1(fieldName=" + this.getFieldName() + ")";
	}
}
class GetterSetterJavadoc2 {
	/**
	 * Some text
	 */
	private int fieldName;
	/**
	 * Some text
	 *
	 * @return Sky is blue2
	 */
	@java.lang.SuppressWarnings("all")
	public int getFieldName() {
		return this.fieldName;
	}
	/**
	 * Some text
	 *
	 * @param fieldName Hello, World2
	 */
	@java.lang.SuppressWarnings("all")
	public void setFieldName(final int fieldName) {
		this.fieldName = fieldName;
	}
}
class GetterSetterJavadoc3 {
	/**
	 * Some text
	 */
	private int fieldName;
	/**
	 * Getter section
	 * @return Sky is blue3
	 */
	@java.lang.SuppressWarnings("all")
	public int getFieldName() {
		return this.fieldName;
	}
	/**
	 * Setter section
	 * @param fieldName Hello, World3
	 */
	@java.lang.SuppressWarnings("all")
	public void setFieldName(final int fieldName) {
		this.fieldName = fieldName;
	}
}
class GetterSetterJavadoc4 {
	/**
	 * Some text
	 */
	private int fieldName;
	/**
	 * Some text
	 * 
	 * @return Sky is blue4
	 */
	@java.lang.SuppressWarnings("all")
	public int fieldName() {
		return this.fieldName;
	}
	/**
	 * Some text
	 * 
	 * @param fieldName Hello, World5
	 * @return {@code this}.
	 */
	@java.lang.SuppressWarnings("all")
	public GetterSetterJavadoc4 fieldName(final int fieldName) {
		this.fieldName = fieldName;
		return this;
	}
}
class GetterSetterJavadoc5 {
	/**
	 * Some text
	 */
	private int fieldName;
	/**
	 * Getter section
	 * @return Sky is blue5
	 */
	@java.lang.SuppressWarnings("all")
	public int fieldName() {
		return this.fieldName;
	}
	/**
	 * Setter section
	 * @param fieldName Hello, World5
	 * @return Sky is blue5
	 */
	@java.lang.SuppressWarnings("all")
	public GetterSetterJavadoc5 fieldName(final int fieldName) {
		this.fieldName = fieldName;
		return this;
	}
}
