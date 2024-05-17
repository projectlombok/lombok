//version 8: Javac 6 will error out due to `ChildBuilder` not existing before properly running lombok. Giving j6 support status, not worth fixing.
import java.util.List;
public class SuperBuilderWithCustomBuilderMethod {
	public static class Parent<A> {
		A field1;
		List<String> items;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ParentBuilder<A, C extends SuperBuilderWithCustomBuilderMethod.Parent<A>, B extends SuperBuilderWithCustomBuilderMethod.Parent.ParentBuilder<A, C, B>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private A field1;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private java.util.ArrayList<String> items;
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
			public B item(final String item) {
				if (this.items == null) this.items = new java.util.ArrayList<String>();
				this.items.add(item);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B items(final java.util.Collection<? extends String> items) {
				if (items == null) {
					throw new java.lang.NullPointerException("items cannot be null");
				}
				if (this.items == null) this.items = new java.util.ArrayList<String>();
				this.items.addAll(items);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B clearItems() {
				if (this.items != null) this.items.clear();
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
				return "SuperBuilderWithCustomBuilderMethod.Parent.ParentBuilder(field1=" + this.field1 + ", items=" + this.items + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ParentBuilderImpl<A> extends SuperBuilderWithCustomBuilderMethod.Parent.ParentBuilder<A, SuperBuilderWithCustomBuilderMethod.Parent<A>, SuperBuilderWithCustomBuilderMethod.Parent.ParentBuilderImpl<A>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ParentBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderWithCustomBuilderMethod.Parent.ParentBuilderImpl<A> self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderWithCustomBuilderMethod.Parent<A> build() {
				return new SuperBuilderWithCustomBuilderMethod.Parent<A>(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Parent(final SuperBuilderWithCustomBuilderMethod.Parent.ParentBuilder<A, ?, ?> b) {
			this.field1 = b.field1;
			java.util.List<String> items;
			switch (b.items == null ? 0 : b.items.size()) {
			case 0: 
				items = java.util.Collections.emptyList();
				break;
			case 1: 
				items = java.util.Collections.singletonList(b.items.get(0));
				break;
			default: 
				items = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(b.items));
			}
			this.items = items;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static <A> SuperBuilderWithCustomBuilderMethod.Parent.ParentBuilder<A, ?, ?> builder() {
			return new SuperBuilderWithCustomBuilderMethod.Parent.ParentBuilderImpl<A>();
		}
	}
	public static class Child<A> extends Parent<A> {
		double field3;
		public static <A> ChildBuilder<A, ?, ?> builder() {
			return new ChildBuilderImpl<A>().item("default item");
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ChildBuilder<A, C extends SuperBuilderWithCustomBuilderMethod.Child<A>, B extends SuperBuilderWithCustomBuilderMethod.Child.ChildBuilder<A, C, B>> extends Parent.ParentBuilder<A, C, B> {
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
				return "SuperBuilderWithCustomBuilderMethod.Child.ChildBuilder(super=" + super.toString() + ", field3=" + this.field3 + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ChildBuilderImpl<A> extends SuperBuilderWithCustomBuilderMethod.Child.ChildBuilder<A, SuperBuilderWithCustomBuilderMethod.Child<A>, SuperBuilderWithCustomBuilderMethod.Child.ChildBuilderImpl<A>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ChildBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderWithCustomBuilderMethod.Child.ChildBuilderImpl<A> self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderWithCustomBuilderMethod.Child<A> build() {
				return new SuperBuilderWithCustomBuilderMethod.Child<A>(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Child(final SuperBuilderWithCustomBuilderMethod.Child.ChildBuilder<A, ?, ?> b) {
			super(b);
			this.field3 = b.field3;
		}
	}
	public static void test() {
		Child<Integer> x = Child.<Integer>builder().field3(0.0).field1(5).item("").build();
	}
}
