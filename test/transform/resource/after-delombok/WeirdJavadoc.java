public class WeirdJavadoc {
	// Comment
	/* Weird comment /** */
	/* Weird comment /** */
	/**
	 * This is the real comment
	 * @param test Copy this
	 */
	WeirdJavadoc(String test) {
	}
	// Comment
	/* Weird comment /** */
	/* Weird comment /** */
	/**
	 * This is the real comment
	 */
	private String test;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class WeirdJavadocBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String test;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		WeirdJavadocBuilder() {
		}
		/**
		 * @param test Copy this
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public WeirdJavadoc.WeirdJavadocBuilder test(final String test) {
			this.test = test;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public WeirdJavadoc build() {
			return new WeirdJavadoc(this.test);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "WeirdJavadoc.WeirdJavadocBuilder(test=" + this.test + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static WeirdJavadoc.WeirdJavadocBuilder builder() {
		return new WeirdJavadoc.WeirdJavadocBuilder();
	}
	/**
	 * This is the real comment
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public String getTest() {
		return this.test;
	}
	/**
	 * This is the real comment
	 * @param test Copy this
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setTest(final String test) {
		this.test = test;
	}
}
