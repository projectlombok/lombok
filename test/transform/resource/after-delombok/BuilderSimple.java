import java.util.List;
class BuilderSimple<T> {
	private final int noshow = 0;
	private final int yes;
	private List<T> also;
	private int $butNotMe;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderSimple(final int yes, final List<T> also) {
		this.yes = yes;
		this.also = also;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected static class BuilderSimpleBuilder<T> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int yes;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private List<T> also;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderSimpleBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderSimple.BuilderSimpleBuilder<T> yes(final int yes) {
			this.yes = yes;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderSimple.BuilderSimpleBuilder<T> also(final List<T> also) {
			this.also = also;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderSimple<T> build() {
			return new BuilderSimple<T>(this.yes, this.also);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderSimple.BuilderSimpleBuilder(yes=" + this.yes + ", also=" + this.also + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected static <T> BuilderSimple.BuilderSimpleBuilder<T> builder() {
		return new BuilderSimple.BuilderSimpleBuilder<T>();
	}
}
