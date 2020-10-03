class WithMethodMarkedDeprecatedAnnOnly {
	@Deprecated
	int annotation;
	WithMethodMarkedDeprecatedAnnOnly(int annotation) {
	}
	@java.lang.Deprecated
	@java.lang.SuppressWarnings("all")
	public WithMethodMarkedDeprecatedAnnOnly withAnnotation(final int annotation) {
		return this.annotation == annotation ? this : new WithMethodMarkedDeprecatedAnnOnly(annotation);
	}
}
