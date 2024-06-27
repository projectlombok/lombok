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
	 * @param last also copy last param
	 */
	BuilderConstructorJavadoc(int basic, int multiline, int predef, int predefWithJavadoc, int last) {
	}
	public static class BuilderConstructorJavadocBuilder<T> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int basic;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int multiline;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int predef;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int predefWithJavadoc;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int last;
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
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderConstructorJavadocBuilder() {
		}
		/**
		 * @param basic tag is moved to the setter
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderConstructorJavadoc.BuilderConstructorJavadocBuilder<T> basic(final int basic) {
			this.basic = basic;
			return this;
		}
		/**
		 * @param multiline a param comment
		 *        can be on multiple lines and can use 
		 *        {@code @code} or <code>tags</code>
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderConstructorJavadoc.BuilderConstructorJavadocBuilder<T> multiline(final int multiline) {
			this.multiline = multiline;
			return this;
		}
		/**
		 * @param last also copy last param
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderConstructorJavadoc.BuilderConstructorJavadocBuilder<T> last(final int last) {
			this.last = last;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderConstructorJavadoc<T> build() {
			return new BuilderConstructorJavadoc<T>(this.basic, this.multiline, this.predef, this.predefWithJavadoc, this.last);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderConstructorJavadoc.BuilderConstructorJavadocBuilder(basic=" + this.basic + ", multiline=" + this.multiline + ", predef=" + this.predef + ", predefWithJavadoc=" + this.predefWithJavadoc + ", last=" + this.last + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static <T> BuilderConstructorJavadoc.BuilderConstructorJavadocBuilder<T> builder() {
		return new BuilderConstructorJavadoc.BuilderConstructorJavadocBuilder<T>();
	}
}
