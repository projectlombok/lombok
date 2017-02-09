class GetterDeprecated {
	@Deprecated
	int annotation;
	/**
	 * @deprecated
	 */
	int javadoc;
	@java.lang.Deprecated
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public int getAnnotation() {
		return this.annotation;
	}
	/**
	 * @deprecated
	 */
	@java.lang.Deprecated
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public int getJavadoc() {
		return this.javadoc;
	}
}
