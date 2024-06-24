//version 8:
public class SuperBuilderWithArrayTypeParam {
	public static class Parent<A> {
		A field1;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ParentBuilder<A, C extends SuperBuilderWithArrayTypeParam.Parent<A>, B extends SuperBuilderWithArrayTypeParam.Parent.ParentBuilder<A, C, B>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private A field1;
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B field1(final A field1) {
				this.field1 = field1;
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
				return "SuperBuilderWithArrayTypeParam.Parent.ParentBuilder(field1=" + this.field1 + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ParentBuilderImpl<A> extends SuperBuilderWithArrayTypeParam.Parent.ParentBuilder<A, SuperBuilderWithArrayTypeParam.Parent<A>, SuperBuilderWithArrayTypeParam.Parent.ParentBuilderImpl<A>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ParentBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderWithArrayTypeParam.Parent.ParentBuilderImpl<A> self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderWithArrayTypeParam.Parent<A> build() {
				return new SuperBuilderWithArrayTypeParam.Parent<A>(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Parent(final SuperBuilderWithArrayTypeParam.Parent.ParentBuilder<A, ?, ?> b) {
			this.field1 = b.field1;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static <A> SuperBuilderWithArrayTypeParam.Parent.ParentBuilder<A, ?, ?> builder() {
			return new SuperBuilderWithArrayTypeParam.Parent.ParentBuilderImpl<A>();
		}
	}
	public static class Child extends Parent<Integer[]> {
		double field3;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ChildBuilder<C extends SuperBuilderWithArrayTypeParam.Child, B extends SuperBuilderWithArrayTypeParam.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<Integer[], C, B> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private double field3;
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B field3(final double field3) {
				this.field3 = field3;
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
				return "SuperBuilderWithArrayTypeParam.Child.ChildBuilder(super=" + super.toString() + ", field3=" + this.field3 + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ChildBuilderImpl extends SuperBuilderWithArrayTypeParam.Child.ChildBuilder<SuperBuilderWithArrayTypeParam.Child, SuperBuilderWithArrayTypeParam.Child.ChildBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ChildBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderWithArrayTypeParam.Child.ChildBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderWithArrayTypeParam.Child build() {
				return new SuperBuilderWithArrayTypeParam.Child(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Child(final SuperBuilderWithArrayTypeParam.Child.ChildBuilder<?, ?> b) {
			super(b);
			this.field3 = b.field3;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderWithArrayTypeParam.Child.ChildBuilder<?, ?> builder() {
			return new SuperBuilderWithArrayTypeParam.Child.ChildBuilderImpl();
		}
	}
	public static void test() {
		Child x = Child.builder().field3(0.0).field1(new Integer[] {2}).build();
	}
}