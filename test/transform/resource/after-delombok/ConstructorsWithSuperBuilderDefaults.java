class ConstructorsWithSuperBuilderDefaults {
	int x;
	int y;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private static int $default$x() {
		return 5;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static abstract class ConstructorsWithSuperBuilderDefaultsBuilder<C extends ConstructorsWithSuperBuilderDefaults, B extends ConstructorsWithSuperBuilderDefaults.ConstructorsWithSuperBuilderDefaultsBuilder<C, B>> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private boolean x$set;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int x$value;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int y;
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public B x(final int x) {
			this.x$value = x;
			x$set = true;
			return self();
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public B y(final int y) {
			this.y = y;
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
			return "ConstructorsWithSuperBuilderDefaults.ConstructorsWithSuperBuilderDefaultsBuilder(x$value=" + this.x$value + ", y=" + this.y + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private static final class ConstructorsWithSuperBuilderDefaultsBuilderImpl extends ConstructorsWithSuperBuilderDefaults.ConstructorsWithSuperBuilderDefaultsBuilder<ConstructorsWithSuperBuilderDefaults, ConstructorsWithSuperBuilderDefaults.ConstructorsWithSuperBuilderDefaultsBuilderImpl> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private ConstructorsWithSuperBuilderDefaultsBuilderImpl() {
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected ConstructorsWithSuperBuilderDefaults.ConstructorsWithSuperBuilderDefaultsBuilderImpl self() {
			return this;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public ConstructorsWithSuperBuilderDefaults build() {
			return new ConstructorsWithSuperBuilderDefaults(this);
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected ConstructorsWithSuperBuilderDefaults(final ConstructorsWithSuperBuilderDefaults.ConstructorsWithSuperBuilderDefaultsBuilder<?, ?> b) {
		if (b.x$set) this.x = b.x$value;
		 else this.x = ConstructorsWithSuperBuilderDefaults.$default$x();
		this.y = b.y;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static ConstructorsWithSuperBuilderDefaults.ConstructorsWithSuperBuilderDefaultsBuilder<?, ?> builder() {
		return new ConstructorsWithSuperBuilderDefaults.ConstructorsWithSuperBuilderDefaultsBuilderImpl();
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public ConstructorsWithSuperBuilderDefaults() {
		this.x = ConstructorsWithSuperBuilderDefaults.$default$x();
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public ConstructorsWithSuperBuilderDefaults(final int x, final int y) {
		this.x = x;
		this.y = y;
	}
}
