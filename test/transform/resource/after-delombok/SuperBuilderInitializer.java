class SuperBuilderInitializer {

	public static class One {
		private String world;

		{
			world = "Hello";
		}

		private static final String world2;

		static {
			world2 = "Hello";
		}


		@java.lang.SuppressWarnings("all")
		public static abstract class OneBuilder<C extends SuperBuilderInitializer.One, B extends SuperBuilderInitializer.One.OneBuilder<C, B>> {
			@java.lang.SuppressWarnings("all")
			private String world;

			@java.lang.SuppressWarnings("all")
			protected abstract B self();

			@java.lang.SuppressWarnings("all")
			public abstract C build();

			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			public B world(final String world) {
				this.world = world;
				return self();
			}

			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "SuperBuilderInitializer.One.OneBuilder(world=" + this.world + ")";
			}
		}


		@java.lang.SuppressWarnings("all")
		private static final class OneBuilderImpl extends SuperBuilderInitializer.One.OneBuilder<SuperBuilderInitializer.One, SuperBuilderInitializer.One.OneBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			private OneBuilderImpl() {
			}

			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected SuperBuilderInitializer.One.OneBuilderImpl self() {
				return this;
			}

			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public SuperBuilderInitializer.One build() {
				return new SuperBuilderInitializer.One(this);
			}
		}

		@java.lang.SuppressWarnings("all")
		protected One(final SuperBuilderInitializer.One.OneBuilder<?, ?> b) {
			this.world = b.world;
		}

		@java.lang.SuppressWarnings("all")
		public static SuperBuilderInitializer.One.OneBuilder<?, ?> builder() {
			return new SuperBuilderInitializer.One.OneBuilderImpl();
		}
	}
}
