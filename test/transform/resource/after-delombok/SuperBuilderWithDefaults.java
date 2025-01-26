import java.util.List;
public class SuperBuilderWithDefaults {
	public static class Parent<N extends Number> {
		private long millis;
		private N numberField;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static <N extends Number> long $default$millis() {
			return System.currentTimeMillis();
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static <N extends Number> N $default$numberField() {
			return null;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ParentBuilder<N extends Number, C extends SuperBuilderWithDefaults.Parent<N>, B extends SuperBuilderWithDefaults.Parent.ParentBuilder<N, C, B>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private boolean millis$set;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private long millis$value;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private boolean numberField$set;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private N numberField$value;
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B millis(final long millis) {
				this.millis$value = millis;
				millis$set = true;
				return self();
			}
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B numberField(final N numberField) {
				this.numberField$value = numberField;
				numberField$set = true;
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
				return "SuperBuilderWithDefaults.Parent.ParentBuilder(millis$value=" + this.millis$value + ", numberField$value=" + this.numberField$value + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ParentBuilderImpl<N extends Number> extends SuperBuilderWithDefaults.Parent.ParentBuilder<N, SuperBuilderWithDefaults.Parent<N>, SuperBuilderWithDefaults.Parent.ParentBuilderImpl<N>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ParentBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderWithDefaults.Parent.ParentBuilderImpl<N> self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderWithDefaults.Parent<N> build() {
				return new SuperBuilderWithDefaults.Parent<N>(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Parent(final SuperBuilderWithDefaults.Parent.ParentBuilder<N, ?, ?> b) {
			if (b.millis$set) this.millis = b.millis$value;
			 else this.millis = SuperBuilderWithDefaults.Parent.<N>$default$millis();
			if (b.numberField$set) this.numberField = b.numberField$value;
			 else this.numberField = SuperBuilderWithDefaults.Parent.<N>$default$numberField();
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static <N extends Number> SuperBuilderWithDefaults.Parent.ParentBuilder<N, ?, ?> builder() {
			return new SuperBuilderWithDefaults.Parent.ParentBuilderImpl<N>();
		}
	}
	public static class Child extends Parent<Integer> {
		private double doubleField;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static double $default$doubleField() {
			return Math.PI;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ChildBuilder<C extends SuperBuilderWithDefaults.Child, B extends SuperBuilderWithDefaults.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<Integer, C, B> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private boolean doubleField$set;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private double doubleField$value;
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B doubleField(final double doubleField) {
				this.doubleField$value = doubleField;
				doubleField$set = true;
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
				return "SuperBuilderWithDefaults.Child.ChildBuilder(super=" + super.toString() + ", doubleField$value=" + this.doubleField$value + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ChildBuilderImpl extends SuperBuilderWithDefaults.Child.ChildBuilder<SuperBuilderWithDefaults.Child, SuperBuilderWithDefaults.Child.ChildBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ChildBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderWithDefaults.Child.ChildBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderWithDefaults.Child build() {
				return new SuperBuilderWithDefaults.Child(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Child(final SuperBuilderWithDefaults.Child.ChildBuilder<?, ?> b) {
			super(b);
			if (b.doubleField$set) this.doubleField = b.doubleField$value;
			 else this.doubleField = SuperBuilderWithDefaults.Child.$default$doubleField();
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderWithDefaults.Child.ChildBuilder<?, ?> builder() {
			return new SuperBuilderWithDefaults.Child.ChildBuilderImpl();
		}
	}
	public static void test() {
		Child x = Child.builder().doubleField(0.1).numberField(5).millis(1234567890L).build();
	}
}
