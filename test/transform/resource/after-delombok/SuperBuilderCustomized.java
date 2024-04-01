import java.util.List;
public class SuperBuilderCustomized {
	public static class Parent<T> {
		public static abstract class ParentBuilder<T, C extends Parent<T>, B extends ParentBuilder<T, C, B>> {
			@java.lang.SuppressWarnings("all")
			private int field1;
			public B resetToDefault() {
				field1 = 0;
				return self();
			}
			public B field1(int field1) {
				this.field1 = field1 + 1;
				return self();
			}
			@java.lang.SuppressWarnings("all")
			protected abstract B self();
			@java.lang.SuppressWarnings("all")
			public abstract C build();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "SuperBuilderCustomized.Parent.ParentBuilder(field1=" + this.field1 + ")";
			}
		}
		int field1;
		protected Parent(ParentBuilder<?, ?, ?> b) {
			if (b.field1 == 0) throw new IllegalArgumentException("field1 must be != 0");
			this.field1 = b.field1;
		}
		public static <T> SuperBuilderCustomized.Parent.ParentBuilder<T, ?, ?> builder(int field1) {
			return new SuperBuilderCustomized.Parent.ParentBuilderImpl<T>().field1(field1);
		}
		@java.lang.SuppressWarnings("all")
		private static final class ParentBuilderImpl<T> extends SuperBuilderCustomized.Parent.ParentBuilder<T, SuperBuilderCustomized.Parent<T>, SuperBuilderCustomized.Parent.ParentBuilderImpl<T>> {
			@java.lang.SuppressWarnings("all")
			private ParentBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected SuperBuilderCustomized.Parent.ParentBuilderImpl<T> self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public SuperBuilderCustomized.Parent<T> build() {
				return new SuperBuilderCustomized.Parent<T>(this);
			}
		}
	}
	public static class Child extends Parent<String> {
		private static final class ChildBuilderImpl extends ChildBuilder<Child, ChildBuilderImpl> {
			@Override
			public Child build() {
				this.resetToDefault();
				return new Child(this);
			}
			@java.lang.SuppressWarnings("all")
			private ChildBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected SuperBuilderCustomized.Child.ChildBuilderImpl self() {
				return this;
			}
		}
		double field2;
		public static ChildBuilder<?, ?> builder() {
			return new ChildBuilderImpl().field2(10.0);
		}
		@java.lang.SuppressWarnings("all")
		public static abstract class ChildBuilder<C extends SuperBuilderCustomized.Child, B extends SuperBuilderCustomized.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<String, C, B> {
			@java.lang.SuppressWarnings("all")
			private double field2;
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			public B field2(final double field2) {
				this.field2 = field2;
				return self();
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected abstract B self();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public abstract C build();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "SuperBuilderCustomized.Child.ChildBuilder(super=" + super.toString() + ", field2=" + this.field2 + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		protected Child(final SuperBuilderCustomized.Child.ChildBuilder<?, ?> b) {
			super(b);
			this.field2 = b.field2;
		}
	}
	public static void test() {
		Child x = Child.builder().field2(1.0).field1(5).resetToDefault().build();
	}
}
