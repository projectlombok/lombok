import java.util.List;
class BuilderWithPrefix<T> {
	private int unprefixed;
	@java.lang.SuppressWarnings("all")
	BuilderWithPrefix(final int unprefixed) {
		this.unprefixed = unprefixed;
	}
	@java.lang.SuppressWarnings("all")
	protected static class BuilderWithPrefixBuilder<T> {
		@java.lang.SuppressWarnings("all")
		private int unprefixed;
		@java.lang.SuppressWarnings("all")
		BuilderWithPrefixBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithPrefixBuilder<T> withUnprefixed(final int unprefixed) {
			this.unprefixed = unprefixed;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithPrefix<T> build() {
			return new BuilderWithPrefix<T>(unprefixed);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderWithPrefix.BuilderWithPrefixBuilder(unprefixed=" + this.unprefixed + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	protected static <T> BuilderWithPrefixBuilder<T> builder() {
		return new BuilderWithPrefixBuilder<T>();
	}
}
