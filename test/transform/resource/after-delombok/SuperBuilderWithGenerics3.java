import java.util.List;
public class SuperBuilderWithGenerics3 {
	public static class Parent<A> {
		private final String str;
		@java.lang.SuppressWarnings("all")
		public static abstract class ParentBuilder<A, C extends Parent<A>, B extends ParentBuilder<A, C, B>> {
			@java.lang.SuppressWarnings("all")
			private String str;
			@java.lang.SuppressWarnings("all")
			protected abstract B self();
			@java.lang.SuppressWarnings("all")
			public abstract C build();
			@java.lang.SuppressWarnings("all")
			public B str(final String str) {
				this.str = str;
				return self();
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "SuperBuilderWithGenerics3.Parent.ParentBuilder(str=" + this.str + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		private static final class ParentBuilderImpl<A> extends ParentBuilder<A, Parent<A>, ParentBuilderImpl<A>> {
			@java.lang.SuppressWarnings("all")
			private ParentBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected ParentBuilderImpl<A> self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public Parent<A> build() {
				return new Parent<A>(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		protected Parent(final ParentBuilder<A, ?, ?> b) {
			this.str = b.str;
		}
		@java.lang.SuppressWarnings("all")
		public static <A> ParentBuilder<A, ?, ?> builder() {
			return new ParentBuilderImpl<A>();
		}
	}
	public static class Child extends Parent<Child.SomeInnerStaticClass> {
		public static class SomeInnerStaticClass {
		}
		double field3;
		@java.lang.SuppressWarnings("all")
		public static abstract class ChildBuilder<C extends Child, B extends ChildBuilder<C, B>> extends Parent.ParentBuilder<Child.SomeInnerStaticClass, C, B> {
			@java.lang.SuppressWarnings("all")
			private double field3;
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected abstract B self();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public abstract C build();
			@java.lang.SuppressWarnings("all")
			public B field3(final double field3) {
				this.field3 = field3;
				return self();
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "SuperBuilderWithGenerics3.Child.ChildBuilder(super=" + super.toString() + ", field3=" + this.field3 + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		private static final class ChildBuilderImpl extends ChildBuilder<Child, ChildBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			private ChildBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected ChildBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public Child build() {
				return new Child(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		protected Child(final ChildBuilder<?, ?> b) {
			super(b);
			this.field3 = b.field3;
		}
		@java.lang.SuppressWarnings("all")
		public static ChildBuilder<?, ?> builder() {
			return new ChildBuilderImpl();
		}
	}
}