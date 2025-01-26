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
		@lombok.Generated
		public static abstract class OneBuilder<C extends SuperBuilderInitializer.One, B extends SuperBuilderInitializer.One.OneBuilder<C, B>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private String world;
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B world(final String world) {
				this.world = world;
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
				return "SuperBuilderInitializer.One.OneBuilder(world=" + this.world + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class OneBuilderImpl extends SuperBuilderInitializer.One.OneBuilder<SuperBuilderInitializer.One, SuperBuilderInitializer.One.OneBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private OneBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderInitializer.One.OneBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderInitializer.One build() {
				return new SuperBuilderInitializer.One(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected One(final SuperBuilderInitializer.One.OneBuilder<?, ?> b) {
			this.world = b.world;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderInitializer.One.OneBuilder<?, ?> builder() {
			return new SuperBuilderInitializer.One.OneBuilderImpl();
		}
	}
}
