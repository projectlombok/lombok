import java.util.List;
public class SuperBuilderWithGenerics2 {
	public static class Parent<A> {
		A field1;
		List<String> items;
		@java.lang.SuppressWarnings("all")
		public static abstract class ParentBuilder<A, C extends SuperBuilderWithGenerics2.Parent<A>, B extends SuperBuilderWithGenerics2.Parent.ParentBuilder<A, C, B>> {
			@java.lang.SuppressWarnings("all")
			private A field1;
			@java.lang.SuppressWarnings("all")
			private java.util.ArrayList<String> items;
			@java.lang.SuppressWarnings("all")
			protected abstract B self();
			@java.lang.SuppressWarnings("all")
			public abstract C build();
			@java.lang.SuppressWarnings("all")
			public B field1(final A field1) {
				this.field1 = field1;
				return self();
			}
			@java.lang.SuppressWarnings("all")
			public B item(final String item) {
				if (this.items == null) this.items = new java.util.ArrayList<String>();
				this.items.add(item);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			public B items(final java.util.Collection<? extends String> items) {
				if (items == null) {
					throw new java.lang.NullPointerException("items cannot be null");
				}
				if (this.items == null) this.items = new java.util.ArrayList<String>();
				this.items.addAll(items);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			public B clearItems() {
				if (this.items != null) this.items.clear();
				return self();
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "SuperBuilderWithGenerics2.Parent.ParentBuilder(field1=" + this.field1 + ", items=" + this.items + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		private static final class ParentBuilderImpl<A> extends SuperBuilderWithGenerics2.Parent.ParentBuilder<A, SuperBuilderWithGenerics2.Parent<A>, SuperBuilderWithGenerics2.Parent.ParentBuilderImpl<A>> {
			@java.lang.SuppressWarnings("all")
			private ParentBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected SuperBuilderWithGenerics2.Parent.ParentBuilderImpl<A> self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public SuperBuilderWithGenerics2.Parent<A> build() {
				return new SuperBuilderWithGenerics2.Parent<A>(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		protected Parent(final SuperBuilderWithGenerics2.Parent.ParentBuilder<A, ?, ?> b) {
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
		public static <A> SuperBuilderWithGenerics2.Parent.ParentBuilder<A, ?, ?> builder() {
			return new SuperBuilderWithGenerics2.Parent.ParentBuilderImpl<A>();
		}
	}
	public static class Child<A> extends Parent<String> {
		A field3;
		@java.lang.SuppressWarnings("all")
		public static abstract class ChildBuilder<A, C extends SuperBuilderWithGenerics2.Child<A>, B extends SuperBuilderWithGenerics2.Child.ChildBuilder<A, C, B>> extends Parent.ParentBuilder<String, C, B> {
			@java.lang.SuppressWarnings("all")
			private A field3;
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected abstract B self();
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public abstract C build();
			@java.lang.SuppressWarnings("all")
			public B field3(final A field3) {
				this.field3 = field3;
				return self();
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "SuperBuilderWithGenerics2.Child.ChildBuilder(super=" + super.toString() + ", field3=" + this.field3 + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		private static final class ChildBuilderImpl<A> extends SuperBuilderWithGenerics2.Child.ChildBuilder<A, SuperBuilderWithGenerics2.Child<A>, SuperBuilderWithGenerics2.Child.ChildBuilderImpl<A>> {
			@java.lang.SuppressWarnings("all")
			private ChildBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected SuperBuilderWithGenerics2.Child.ChildBuilderImpl<A> self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public SuperBuilderWithGenerics2.Child<A> build() {
				return new SuperBuilderWithGenerics2.Child<A>(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		protected Child(final SuperBuilderWithGenerics2.Child.ChildBuilder<A, ?, ?> b) {
			super(b);
			this.field3 = b.field3;
		}
		@java.lang.SuppressWarnings("all")
		public static <A> SuperBuilderWithGenerics2.Child.ChildBuilder<A, ?, ?> builder2() {
			return new SuperBuilderWithGenerics2.Child.ChildBuilderImpl<A>();
		}
	}
	public static void test() {
		Child<Integer> x = Child.<Integer>builder2().field3(1).field1("value").item("").build();
	}
}
