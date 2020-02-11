public class SuperBuilderNestedGenericTypes {
	public static abstract class Generic<T extends Generic<?>> {
		@java.lang.SuppressWarnings("all")
		public static abstract class GenericBuilder<T extends Generic<?>, C extends SuperBuilderNestedGenericTypes.Generic<T>, B extends SuperBuilderNestedGenericTypes.Generic.GenericBuilder<T, C, B>> {
			@java.lang.SuppressWarnings("all")
			protected abstract B self();
			@java.lang.SuppressWarnings("all")
			public abstract C build();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "SuperBuilderNestedGenericTypes.Generic.GenericBuilder()";
			}
		}
		@java.lang.SuppressWarnings("all")
		protected Generic(final SuperBuilderNestedGenericTypes.Generic.GenericBuilder<T, ?, ?> b) {
		}
	}
	public static abstract class NestedGeneric<T extends OtherGeneric<?>> extends Generic<NestedGeneric<? extends OtherGeneric<?>>> {
		@java.lang.SuppressWarnings("all")
		public static abstract class NestedGenericBuilder<T extends OtherGeneric<?>, C extends SuperBuilderNestedGenericTypes.NestedGeneric<T>, B extends SuperBuilderNestedGenericTypes.NestedGeneric.NestedGenericBuilder<T, C, B>> extends Generic.GenericBuilder<NestedGeneric<? extends OtherGeneric<?>>, C, B> {
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected abstract B self();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public abstract C build();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "SuperBuilderNestedGenericTypes.NestedGeneric.NestedGenericBuilder(super=" + super.toString() + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		protected NestedGeneric(final SuperBuilderNestedGenericTypes.NestedGeneric.NestedGenericBuilder<T, ?, ?> b) {
			super(b);
		}
	}
	public interface OtherGeneric<T> {
	}
}
