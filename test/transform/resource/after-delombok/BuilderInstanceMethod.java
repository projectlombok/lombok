import java.util.List;
class BuilderInstanceMethod<T> {
	public String create(int show, final int yes, List<T> also, int $andMe) {
		return "" + show + yes + also + $andMe;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public class StringBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int show;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int yes;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private List<T> also;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int $andMe;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		StringBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderInstanceMethod<T>.StringBuilder show(final int show) {
			this.show = show;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderInstanceMethod<T>.StringBuilder yes(final int yes) {
			this.yes = yes;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderInstanceMethod<T>.StringBuilder also(final List<T> also) {
			this.also = also;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderInstanceMethod<T>.StringBuilder $andMe(final int $andMe) {
			this.$andMe = $andMe;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public String build() {
			return BuilderInstanceMethod.this.create(this.show, this.yes, this.also, this.$andMe);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderInstanceMethod.StringBuilder(show=" + this.show + ", yes=" + this.yes + ", also=" + this.also + ", $andMe=" + this.$andMe + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public BuilderInstanceMethod<T>.StringBuilder builder() {
		return this.new StringBuilder();
	}
}
