import java.util.List;
class SuperBuilderCustomName<T> {
	private final int field;
	@java.lang.SuppressWarnings("all")
	public static abstract class SimpleTestBuilder<T, C extends SuperBuilderCustomName<T>, B extends SuperBuilderCustomName.SimpleTestBuilder<T, C, B>> {
		@java.lang.SuppressWarnings("all")
		private int field;
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public B field(final int field) {
			this.field = field;
			return self();
		}
		@java.lang.SuppressWarnings("all")
		protected abstract B self();
		@java.lang.SuppressWarnings("all")
		public abstract C build();
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "SuperBuilderCustomName.SimpleTestBuilder(field=" + this.field + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	private static final class SimpleTestBuilderImpl<T> extends SuperBuilderCustomName.SimpleTestBuilder<T, SuperBuilderCustomName<T>, SuperBuilderCustomName.SimpleTestBuilderImpl<T>> {
		@java.lang.SuppressWarnings("all")
		private SimpleTestBuilderImpl() {
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		protected SuperBuilderCustomName.SimpleTestBuilderImpl<T> self() {
			return this;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public SuperBuilderCustomName<T> build() {
			return new SuperBuilderCustomName<T>(this);
		}
	}
	@java.lang.SuppressWarnings("all")
	protected SuperBuilderCustomName(final SuperBuilderCustomName.SimpleTestBuilder<T, ?, ?> b) {
		this.field = b.field;
	}
	@java.lang.SuppressWarnings("all")
	public static <T> SuperBuilderCustomName.SimpleTestBuilder<T, ?, ?> builder() {
		return new SuperBuilderCustomName.SimpleTestBuilderImpl<T>();
	}
}