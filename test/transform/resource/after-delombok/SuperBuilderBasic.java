import java.util.List;

public class SuperBuilderBasic {
	public static class Parent {
		int field1;
		List<String> items;
		protected Parent(final ParentBuilder<?, ?> b) {
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
		private static class ParentBuilderImpl extends ParentBuilder<Parent, ParentBuilderImpl> {
			@Override
			public Parent build() {
				return new Parent(this);
			}
			
			@Override
			public ParentBuilderImpl self() {
				return this;
			}
		}
		public abstract static class ParentBuilder<C extends Parent, B extends ParentBuilder<C, B>> {
			@java.lang.SuppressWarnings("all")
			private int field1;
			@java.lang.SuppressWarnings("all")
			private java.util.ArrayList<String> items;
			@java.lang.SuppressWarnings("all")
			ParentBuilder() {
			}
			@java.lang.SuppressWarnings("all")
			public B field1(final int field1) {
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
				if (this.items == null) this.items = new java.util.ArrayList<String>();
				this.items.addAll(items);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			public B clearItems() {
				if (this.items != null) this.items.clear();
				return self();
			}
			@java.lang.SuppressWarnings("all")
			protected abstract B self();
			@java.lang.SuppressWarnings("all")
			public abstract C build();
		}
		public static ParentBuilder<?, ?> builder() {
			return new ParentBuilderImpl();
		}
	}
	public static class Child extends Parent {
		double field3;
		protected Child(ChildBuilder<?, ?> b) {
			super(b);
			this.field3 = b.field3;
		}
		private static class ChildBuilderImpl extends ChildBuilder<Child, ChildBuilderImpl> {
			@Override
			public Child build() {
				return new Child(this);
			}
			
			@Override
			public ChildBuilderImpl self() {
				return this;
			}
		}
		public abstract static class ChildBuilder<C extends Child, B extends ChildBuilder<C, B>> extends Parent.ParentBuilder<Child, B> {
			@java.lang.SuppressWarnings("all")
			private double field3;
			@java.lang.SuppressWarnings("all")
			ChildBuilder() {
			}
			@java.lang.SuppressWarnings("all")
			public B field3(final double field3) {
				this.field3 = field3;
				return self();
			}
			@java.lang.SuppressWarnings("all")
			protected abstract B self();
			@java.lang.SuppressWarnings("all")
			public abstract C build();
		}
		public static ChildBuilder<?, ?> builder() {
			return new ChildBuilderImpl();
		}
	}
	public static void test() {
		Child x = Child.builder().field3(0.0).field1(5).item("").build();
	}
}
