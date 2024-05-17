public class SuperBuilderAbstract {
	public static class Parent {
		int parentField;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ParentBuilder<C extends SuperBuilderAbstract.Parent, B extends SuperBuilderAbstract.Parent.ParentBuilder<C, B>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private int parentField;
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B parentField(final int parentField) {
				this.parentField = parentField;
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
				return "SuperBuilderAbstract.Parent.ParentBuilder(parentField=" + this.parentField + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ParentBuilderImpl extends SuperBuilderAbstract.Parent.ParentBuilder<SuperBuilderAbstract.Parent, SuperBuilderAbstract.Parent.ParentBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ParentBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderAbstract.Parent.ParentBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderAbstract.Parent build() {
				return new SuperBuilderAbstract.Parent(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Parent(final SuperBuilderAbstract.Parent.ParentBuilder<?, ?> b) {
			this.parentField = b.parentField;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderAbstract.Parent.ParentBuilder<?, ?> builder() {
			return new SuperBuilderAbstract.Parent.ParentBuilderImpl();
		}
	}
	public static abstract class Child extends Parent {
		double childField;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ChildBuilder<C extends SuperBuilderAbstract.Child, B extends SuperBuilderAbstract.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private double childField;
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B childField(final double childField) {
				this.childField = childField;
				return self();
			}
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
				return "SuperBuilderAbstract.Child.ChildBuilder(super=" + super.toString() + ", childField=" + this.childField + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Child(final SuperBuilderAbstract.Child.ChildBuilder<?, ?> b) {
			super(b);
			this.childField = b.childField;
		}
	}
	public static class GrandChild extends Child {
		String grandChildField;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class GrandChildBuilder<C extends SuperBuilderAbstract.GrandChild, B extends SuperBuilderAbstract.GrandChild.GrandChildBuilder<C, B>> extends Child.ChildBuilder<C, B> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private String grandChildField;
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B grandChildField(final String grandChildField) {
				this.grandChildField = grandChildField;
				return self();
			}
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
				return "SuperBuilderAbstract.GrandChild.GrandChildBuilder(super=" + super.toString() + ", grandChildField=" + this.grandChildField + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class GrandChildBuilderImpl extends SuperBuilderAbstract.GrandChild.GrandChildBuilder<SuperBuilderAbstract.GrandChild, SuperBuilderAbstract.GrandChild.GrandChildBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private GrandChildBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderAbstract.GrandChild.GrandChildBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderAbstract.GrandChild build() {
				return new SuperBuilderAbstract.GrandChild(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected GrandChild(final SuperBuilderAbstract.GrandChild.GrandChildBuilder<?, ?> b) {
			super(b);
			this.grandChildField = b.grandChildField;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderAbstract.GrandChild.GrandChildBuilder<?, ?> builder() {
			return new SuperBuilderAbstract.GrandChild.GrandChildBuilderImpl();
		}
	}
	public static void test() {
		GrandChild x = GrandChild.builder().grandChildField("").parentField(5).childField(2.5).build();
	}
}
