class SuperBuilderWithCustomBuilderClassName {
	static class SuperClass {
		@java.lang.SuppressWarnings("all")
		public static abstract class Builder<C extends SuperBuilderWithCustomBuilderClassName.SuperClass, B extends SuperBuilderWithCustomBuilderClassName.SuperClass.Builder<C, B>> {
			@java.lang.SuppressWarnings("all")
			protected abstract B self();
			@java.lang.SuppressWarnings("all")
			public abstract C build();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "SuperBuilderWithCustomBuilderClassName.SuperClass.Builder()";
			}
		}
		@java.lang.SuppressWarnings("all")
		private static final class BuilderImpl extends SuperBuilderWithCustomBuilderClassName.SuperClass.Builder<SuperBuilderWithCustomBuilderClassName.SuperClass, SuperBuilderWithCustomBuilderClassName.SuperClass.BuilderImpl> {
			@java.lang.SuppressWarnings("all")
			private BuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected SuperBuilderWithCustomBuilderClassName.SuperClass.BuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public SuperBuilderWithCustomBuilderClassName.SuperClass build() {
				return new SuperBuilderWithCustomBuilderClassName.SuperClass(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		protected SuperClass(final SuperBuilderWithCustomBuilderClassName.SuperClass.Builder<?, ?> b) {
		}
		@java.lang.SuppressWarnings("all")
		public static SuperBuilderWithCustomBuilderClassName.SuperClass.Builder<?, ?> builder() {
			return new SuperBuilderWithCustomBuilderClassName.SuperClass.BuilderImpl();
		}
	}
	static class SubClass extends SuperClass {
		@java.lang.SuppressWarnings("all")
		public static abstract class Builder<C extends SuperBuilderWithCustomBuilderClassName.SubClass, B extends SuperBuilderWithCustomBuilderClassName.SubClass.Builder<C, B>> extends SuperClass.Builder<C, B> {
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected abstract B self();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public abstract C build();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "SuperBuilderWithCustomBuilderClassName.SubClass.Builder(super=" + super.toString() + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		private static final class BuilderImpl extends SuperBuilderWithCustomBuilderClassName.SubClass.Builder<SuperBuilderWithCustomBuilderClassName.SubClass, SuperBuilderWithCustomBuilderClassName.SubClass.BuilderImpl> {
			@java.lang.SuppressWarnings("all")
			private BuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected SuperBuilderWithCustomBuilderClassName.SubClass.BuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public SuperBuilderWithCustomBuilderClassName.SubClass build() {
				return new SuperBuilderWithCustomBuilderClassName.SubClass(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		protected SubClass(final SuperBuilderWithCustomBuilderClassName.SubClass.Builder<?, ?> b) {
			super(b);
		}
		@java.lang.SuppressWarnings("all")
		public static SuperBuilderWithCustomBuilderClassName.SubClass.Builder<?, ?> builder() {
			return new SuperBuilderWithCustomBuilderClassName.SubClass.BuilderImpl();
		}
	}
}