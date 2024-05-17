class SuperBuilderWithCustomBuilderClassName {
	static class SuperClass {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class Builder<C extends SuperBuilderWithCustomBuilderClassName.SuperClass, B extends SuperBuilderWithCustomBuilderClassName.SuperClass.Builder<C, B>> {
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
				return "SuperBuilderWithCustomBuilderClassName.SuperClass.Builder()";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class BuilderImpl extends SuperBuilderWithCustomBuilderClassName.SuperClass.Builder<SuperBuilderWithCustomBuilderClassName.SuperClass, SuperBuilderWithCustomBuilderClassName.SuperClass.BuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private BuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderWithCustomBuilderClassName.SuperClass.BuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderWithCustomBuilderClassName.SuperClass build() {
				return new SuperBuilderWithCustomBuilderClassName.SuperClass(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected SuperClass(final SuperBuilderWithCustomBuilderClassName.SuperClass.Builder<?, ?> b) {
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderWithCustomBuilderClassName.SuperClass.Builder<?, ?> builder() {
			return new SuperBuilderWithCustomBuilderClassName.SuperClass.BuilderImpl();
		}
	}
	static class SubClass extends SuperClass {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class Builder<C extends SuperBuilderWithCustomBuilderClassName.SubClass, B extends SuperBuilderWithCustomBuilderClassName.SubClass.Builder<C, B>> extends SuperClass.Builder<C, B> {
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected abstract B self();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public abstract C build();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public java.lang.String toString() {
				return "SuperBuilderWithCustomBuilderClassName.SubClass.Builder(super=" + super.toString() + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class BuilderImpl extends SuperBuilderWithCustomBuilderClassName.SubClass.Builder<SuperBuilderWithCustomBuilderClassName.SubClass, SuperBuilderWithCustomBuilderClassName.SubClass.BuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private BuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderWithCustomBuilderClassName.SubClass.BuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderWithCustomBuilderClassName.SubClass build() {
				return new SuperBuilderWithCustomBuilderClassName.SubClass(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected SubClass(final SuperBuilderWithCustomBuilderClassName.SubClass.Builder<?, ?> b) {
			super(b);
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderWithCustomBuilderClassName.SubClass.Builder<?, ?> builder() {
			return new SuperBuilderWithCustomBuilderClassName.SubClass.BuilderImpl();
		}
	}
}
