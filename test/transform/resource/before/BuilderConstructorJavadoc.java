import java.util.List;

class BuilderConstructorJavadoc<T> {
	/**
	 * This is a comment
	 * 
	 * @param basic tag is moved to the setter
	 * @param multiline a param comment
	 *        can be on multiple lines and can use 
	 *        {@code @code} or <code>tags</code>
	 * @param predef don't copy this one
	 * @param predefWithJavadoc don't copy this one
	 */
	@lombok.Builder
	BuilderConstructorJavadoc(int basic, int multiline, int predef, int predefWithJavadoc) {
		
	}
	
	public static class BuilderConstructorJavadocBuilder<T> {
		public BuilderConstructorJavadocBuilder<T> predef(final int x) {
			this.predef = x;
			return this;
		}

		/**
		 * This javadoc remains untouched.
		 * @param x 1/100 of the thing
		 * @return the updated builder
		 */
		public BuilderConstructorJavadocBuilder<T> predefWithJavadoc(final int x) {
			this.predefWithJavadoc = x;
			return this;
		}
	}
}
