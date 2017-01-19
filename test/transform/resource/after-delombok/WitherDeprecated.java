class WitherDeprecated {
	@Deprecated
	int annotation;
	/**
	 * @deprecated
	 */
	int javadoc;
	WitherDeprecated(int annotation, int javadoc) {
	}
	@java.lang.Deprecated
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	public WitherDeprecated withAnnotation(final int annotation) {
		return this.annotation == annotation ? this : new WitherDeprecated(annotation, this.javadoc);
	}
	/**
	 * @deprecated
	 */
	@java.lang.Deprecated
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	public WitherDeprecated withJavadoc(final int javadoc) {
		return this.javadoc == javadoc ? this : new WitherDeprecated(this.annotation, javadoc);
	}
}