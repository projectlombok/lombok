class ConstructorsWithSuperBuilderDefaults {
	int x;
	int y;

	@java.lang.SuppressWarnings("all")
	private static int $default$x() {
		return 5;
	}


	@java.lang.SuppressWarnings("all")
	public static abstract class ConstructorsWithSuperBuilderDefaultsBuilder<C extends ConstructorsWithSuperBuilderDefaults, B extends ConstructorsWithSuperBuilderDefaults.ConstructorsWithSuperBuilderDefaultsBuilder<C, B>> {
		@java.lang.SuppressWarnings("all")
		private boolean x$set;
		@java.lang.SuppressWarnings("all")
		private int x$value;
		@java.lang.SuppressWarnings("all")
		private int y;

		@java.lang.SuppressWarnings("all")
		protected abstract B self();

		@java.lang.SuppressWarnings("all")
		public abstract C build();

		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public B x(final int x) {
			this.x$value = x;
			x$set = true;
			return self();
		}

		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public B y(final int y) {
			this.y = y;
			return self();
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "ConstructorsWithSuperBuilderDefaults.ConstructorsWithSuperBuilderDefaultsBuilder(x$value=" + this.x$value + ", y=" + this.y + ")";
		}
	}


	@java.lang.SuppressWarnings("all")
	private static final class ConstructorsWithSuperBuilderDefaultsBuilderImpl extends ConstructorsWithSuperBuilderDefaults.ConstructorsWithSuperBuilderDefaultsBuilder<ConstructorsWithSuperBuilderDefaults, ConstructorsWithSuperBuilderDefaults.ConstructorsWithSuperBuilderDefaultsBuilderImpl> {
		@java.lang.SuppressWarnings("all")
		private ConstructorsWithSuperBuilderDefaultsBuilderImpl() {
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		protected ConstructorsWithSuperBuilderDefaults.ConstructorsWithSuperBuilderDefaultsBuilderImpl self() {
			return this;
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public ConstructorsWithSuperBuilderDefaults build() {
			return new ConstructorsWithSuperBuilderDefaults(this);
		}
	}

	@java.lang.SuppressWarnings("all")
	protected ConstructorsWithSuperBuilderDefaults(final ConstructorsWithSuperBuilderDefaults.ConstructorsWithSuperBuilderDefaultsBuilder<?, ?> b) {
		if (b.x$set) this.x = b.x$value;
		 else this.x = ConstructorsWithSuperBuilderDefaults.$default$x();
		this.y = b.y;
	}

	@java.lang.SuppressWarnings("all")
	public static ConstructorsWithSuperBuilderDefaults.ConstructorsWithSuperBuilderDefaultsBuilder<?, ?> builder() {
		return new ConstructorsWithSuperBuilderDefaults.ConstructorsWithSuperBuilderDefaultsBuilderImpl();
	}

	@java.lang.SuppressWarnings("all")
	public ConstructorsWithSuperBuilderDefaults() {
		this.x = ConstructorsWithSuperBuilderDefaults.$default$x();
	}

	@java.lang.SuppressWarnings("all")
	public ConstructorsWithSuperBuilderDefaults(final int x, final int y) {
		this.x = x;
		this.y = y;
	}
}