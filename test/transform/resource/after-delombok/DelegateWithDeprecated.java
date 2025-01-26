class DelegateWithDeprecated {
	private Bar bar;
	private interface Bar {
		@Deprecated
		void deprecatedAnnotation();
		/**
		 * @deprecated
		 */
		void deprecatedComment();
		void notDeprecated();
	}
	@java.lang.Deprecated
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void deprecatedAnnotation() {
		this.bar.deprecatedAnnotation();
	}
	@java.lang.Deprecated
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void deprecatedComment() {
		this.bar.deprecatedComment();
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void notDeprecated() {
		this.bar.notDeprecated();
	}
}
