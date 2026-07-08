class SuperBuilderClassNameCollisionQualified<T> {
	T value;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static abstract class SuperBuilder<T, C extends SuperBuilderClassNameCollisionQualified<T>, B extends SuperBuilderClassNameCollisionQualified.SuperBuilder<T, C, B>> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private T value;
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public B value(final T value) {
			this.value = value;
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
			return "SuperBuilderClassNameCollisionQualified.SuperBuilder(value=" + this.value + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private static final class SuperBuilderImpl<T> extends SuperBuilderClassNameCollisionQualified.SuperBuilder<T, SuperBuilderClassNameCollisionQualified<T>, SuperBuilderClassNameCollisionQualified.SuperBuilderImpl<T>> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private SuperBuilderImpl() {
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected SuperBuilderClassNameCollisionQualified.SuperBuilderImpl<T> self() {
			return this;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public SuperBuilderClassNameCollisionQualified<T> build() {
			return new SuperBuilderClassNameCollisionQualified<T>(this);
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected SuperBuilderClassNameCollisionQualified(final SuperBuilderClassNameCollisionQualified.SuperBuilder<T, ?, ?> b) {
		this.value = b.value;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static <T> SuperBuilderClassNameCollisionQualified.SuperBuilder<T, ?, ?> builder() {
		return new SuperBuilderClassNameCollisionQualified.SuperBuilderImpl<T>();
	}
}
