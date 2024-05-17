class GetterDeprecated {
	@Deprecated
	int annotation;
	/**
	 * @deprecated
	 */
	int javadoc;
	@java.lang.Deprecated
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int getAnnotation() {
		return this.annotation;
	}
	/**
	 * @deprecated
	 */
	@java.lang.Deprecated
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int getJavadoc() {
		return this.javadoc;
	}
}
