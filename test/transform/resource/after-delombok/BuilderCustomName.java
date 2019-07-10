import java.util.List;
class BuilderCustomName<T> {
	private final int field;
	@java.lang.SuppressWarnings("all")
	public static abstract class SimpleTestBuilder<T, C extends BuilderCustomName<T>, B extends SimpleTestBuilder<T, C, B>> {
		@java.lang.SuppressWarnings("all")
		private int field;
		@java.lang.SuppressWarnings("all")
		protected abstract B self();
		@java.lang.SuppressWarnings("all")
		public abstract C build();
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
	private static final class SimpleTestBuilderImpl<T> extends SimpleTestBuilder<T, BuilderCustomName<T>, SimpleTestBuilderImpl<T>> {
		@java.lang.SuppressWarnings("all")
		private SimpleTestBuilderImpl() {
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		protected SimpleTestBuilderImpl<T> self() {
			return this;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public BuilderCustomName<T> build() {
			return new BuilderCustomName<T>(this);
		}
	}
	@java.lang.SuppressWarnings("all")
	protected BuilderCustomName(final SimpleTestBuilder<T, ?, ?> b) {
		this.field = b.field;
	}
	@java.lang.SuppressWarnings("all")
	public static <T> SimpleTestBuilder<T, ?, ?> builder() {
		return new SimpleTestBuilderImpl<T>();
	}
}