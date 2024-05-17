public class SuperBuilderWithExistingConstructor {
	public SuperBuilderWithExistingConstructor() {
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static abstract class SuperBuilderWithExistingConstructorBuilder<C extends SuperBuilderWithExistingConstructor, B extends SuperBuilderWithExistingConstructor.SuperBuilderWithExistingConstructorBuilder<C, B>> {
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
			return "SuperBuilderWithExistingConstructor.SuperBuilderWithExistingConstructorBuilder()";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private static final class SuperBuilderWithExistingConstructorBuilderImpl extends SuperBuilderWithExistingConstructor.SuperBuilderWithExistingConstructorBuilder<SuperBuilderWithExistingConstructor, SuperBuilderWithExistingConstructor.SuperBuilderWithExistingConstructorBuilderImpl> {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private SuperBuilderWithExistingConstructorBuilderImpl() {
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected SuperBuilderWithExistingConstructor.SuperBuilderWithExistingConstructorBuilderImpl self() {
			return this;
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public SuperBuilderWithExistingConstructor build() {
			return new SuperBuilderWithExistingConstructor(this);
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected SuperBuilderWithExistingConstructor(final SuperBuilderWithExistingConstructor.SuperBuilderWithExistingConstructorBuilder<?, ?> b) {
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static SuperBuilderWithExistingConstructor.SuperBuilderWithExistingConstructorBuilder<?, ?> builder() {
		return new SuperBuilderWithExistingConstructor.SuperBuilderWithExistingConstructorBuilderImpl();
	}
}
