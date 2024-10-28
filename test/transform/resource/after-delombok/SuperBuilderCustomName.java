import java.util.List;
class SuperBuilderCustomName<T> {
	private final int field;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static abstract class SimpleTestBuilder<T, C extends SuperBuilderCustomName<T>, B extends SuperBuilderCustomName.SimpleTestBuilder<T, C, B>> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int field;
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public B field(final int field) {
			this.field = field;
			return self();
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected abstract B self();
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public abstract C build();
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "SuperBuilderCustomName.SimpleTestBuilder(field=" + this.field + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private static final class SimpleTestBuilderImpl<T> extends SuperBuilderCustomName.SimpleTestBuilder<T, SuperBuilderCustomName<T>, SuperBuilderCustomName.SimpleTestBuilderImpl<T>> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private SimpleTestBuilderImpl() {
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected SuperBuilderCustomName.SimpleTestBuilderImpl<T> self() {
			return this;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public SuperBuilderCustomName<T> build() {
			return new SuperBuilderCustomName<T>(this);
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected SuperBuilderCustomName(final SuperBuilderCustomName.SimpleTestBuilder<T, ?, ?> b) {
		this.field = b.field;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static <T> SuperBuilderCustomName.SimpleTestBuilder<T, ?, ?> builder() {
		return new SuperBuilderCustomName.SimpleTestBuilderImpl<T>();
	}
}
