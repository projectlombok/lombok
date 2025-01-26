class WithMethodMarkedDeprecatedAnnOnly {
	@Deprecated
	int annotation;
	WithMethodMarkedDeprecatedAnnOnly(int annotation) {
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.Deprecated
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public WithMethodMarkedDeprecatedAnnOnly withAnnotation(final int annotation) {
		return this.annotation == annotation ? this : new WithMethodMarkedDeprecatedAnnOnly(annotation);
	}
}
