public class SuperBuilderWithOverloadedGeneratedMethods {
	public static class Parent {
		int self;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ParentBuilder<C extends SuperBuilderWithOverloadedGeneratedMethods.Parent, B extends SuperBuilderWithOverloadedGeneratedMethods.Parent.ParentBuilder<C, B>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private int self;
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B self(final int self) {
				this.self = self;
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
				return "SuperBuilderWithOverloadedGeneratedMethods.Parent.ParentBuilder(self=" + this.self + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ParentBuilderImpl extends SuperBuilderWithOverloadedGeneratedMethods.Parent.ParentBuilder<SuperBuilderWithOverloadedGeneratedMethods.Parent, SuperBuilderWithOverloadedGeneratedMethods.Parent.ParentBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ParentBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderWithOverloadedGeneratedMethods.Parent.ParentBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderWithOverloadedGeneratedMethods.Parent build() {
				return new SuperBuilderWithOverloadedGeneratedMethods.Parent(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Parent(final SuperBuilderWithOverloadedGeneratedMethods.Parent.ParentBuilder<?, ?> b) {
			this.self = b.self;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderWithOverloadedGeneratedMethods.Parent.ParentBuilder<?, ?> builder() {
			return new SuperBuilderWithOverloadedGeneratedMethods.Parent.ParentBuilderImpl();
		}
	}
	public static class Child extends Parent {
		double build;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ChildBuilder<C extends SuperBuilderWithOverloadedGeneratedMethods.Child, B extends SuperBuilderWithOverloadedGeneratedMethods.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private double build;
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B build(final double build) {
				this.build = build;
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
				return "SuperBuilderWithOverloadedGeneratedMethods.Child.ChildBuilder(super=" + super.toString() + ", build=" + this.build + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ChildBuilderImpl extends SuperBuilderWithOverloadedGeneratedMethods.Child.ChildBuilder<SuperBuilderWithOverloadedGeneratedMethods.Child, SuperBuilderWithOverloadedGeneratedMethods.Child.ChildBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ChildBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderWithOverloadedGeneratedMethods.Child.ChildBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderWithOverloadedGeneratedMethods.Child build() {
				return new SuperBuilderWithOverloadedGeneratedMethods.Child(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Child(final SuperBuilderWithOverloadedGeneratedMethods.Child.ChildBuilder<?, ?> b) {
			super(b);
			this.build = b.build;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderWithOverloadedGeneratedMethods.Child.ChildBuilder<?, ?> builder() {
			return new SuperBuilderWithOverloadedGeneratedMethods.Child.ChildBuilderImpl();
		}
	}
	public static void test() {
		Child x = Child.builder().build(0.0).self(5).build();
	}
}
