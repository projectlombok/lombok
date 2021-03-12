public class SuperBuilderWithExistingConstuctor {
	public SuperBuilderWithExistingConstuctor() {
	}
	@java.lang.SuppressWarnings("all")
	public static abstract class SuperBuilderWithExistingConstuctorBuilder<C extends SuperBuilderWithExistingConstuctor, B extends SuperBuilderWithExistingConstuctor.SuperBuilderWithExistingConstuctorBuilder<C, B>> {
		@java.lang.SuppressWarnings("all")
		protected abstract B self();
		@java.lang.SuppressWarnings("all")
		public abstract C build();
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "SuperBuilderWithExistingConstuctor.SuperBuilderWithExistingConstuctorBuilder()";
		}
	}
	@java.lang.SuppressWarnings("all")
	private static final class SuperBuilderWithExistingConstuctorBuilderImpl extends SuperBuilderWithExistingConstuctor.SuperBuilderWithExistingConstuctorBuilder<SuperBuilderWithExistingConstuctor, SuperBuilderWithExistingConstuctor.SuperBuilderWithExistingConstuctorBuilderImpl> {
		@java.lang.SuppressWarnings("all")
		private SuperBuilderWithExistingConstuctorBuilderImpl() {
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		protected SuperBuilderWithExistingConstuctor.SuperBuilderWithExistingConstuctorBuilderImpl self() {
			return this;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public SuperBuilderWithExistingConstuctor build() {
			return new SuperBuilderWithExistingConstuctor(this);
		}
	}
	@java.lang.SuppressWarnings("all")
	protected SuperBuilderWithExistingConstuctor(final SuperBuilderWithExistingConstuctor.SuperBuilderWithExistingConstuctorBuilder<?, ?> b) {
	}
	@java.lang.SuppressWarnings("all")
	public static SuperBuilderWithExistingConstuctor.SuperBuilderWithExistingConstuctorBuilder<?, ?> builder() {
		return new SuperBuilderWithExistingConstuctor.SuperBuilderWithExistingConstuctorBuilderImpl();
	}
}