import java.util.List;
class BuilderCustomName<T> {
	private final int field;
	@java.lang.SuppressWarnings("all")
	public static abstract class SimpleTestBuilder<T, C extends BuilderCustomName<T>, B extends BuilderCustomName.SimpleTestBuilder<T, C, B>> {
		@java.lang.SuppressWarnings("all")
		private int field;
		@java.lang.SuppressWarnings("all")
		protected abstract B self();
		@java.lang.SuppressWarnings("all")
		public abstract C build();
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public B field(final int field) {
			this.field = field;
			return self();
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderCustomName.SimpleTestBuilder(field=" + this.field + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	private static final class SimpleTestBuilderImpl<T> extends BuilderCustomName.SimpleTestBuilder<T, BuilderCustomName<T>, BuilderCustomName.SimpleTestBuilderImpl<T>> {
		@java.lang.SuppressWarnings("all")
		private SimpleTestBuilderImpl() {
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		protected BuilderCustomName.SimpleTestBuilderImpl<T> self() {
			return this;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public BuilderCustomName<T> build() {
			return new BuilderCustomName<T>(this);
		}
	}
	@java.lang.SuppressWarnings("all")
	protected BuilderCustomName(final BuilderCustomName.SimpleTestBuilder<T, ?, ?> b) {
		this.field = b.field;
	}
	@java.lang.SuppressWarnings("all")
	public static <T> BuilderCustomName.SimpleTestBuilder<T, ?, ?> builder() {
		return new BuilderCustomName.SimpleTestBuilderImpl<T>();
	}
}