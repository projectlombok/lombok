import java.util.List;
public class SuperBuilderBasic {
	public static class Parent {
		int field1;
		List<String> items;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ParentBuilder<C extends SuperBuilderBasic.Parent, B extends SuperBuilderBasic.Parent.ParentBuilder<C, B>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private int field1;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private java.util.ArrayList<String> items;
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B field1(final int field1) {
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
				return "SuperBuilderBasic.Parent.ParentBuilder(field1=" + this.field1 + ", items=" + this.items + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ParentBuilderImpl extends SuperBuilderBasic.Parent.ParentBuilder<SuperBuilderBasic.Parent, SuperBuilderBasic.Parent.ParentBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ParentBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderBasic.Parent.ParentBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderBasic.Parent build() {
				return new SuperBuilderBasic.Parent(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Parent(final SuperBuilderBasic.Parent.ParentBuilder<?, ?> b) {
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
		public static SuperBuilderBasic.Parent.ParentBuilder<?, ?> builder() {
			return new SuperBuilderBasic.Parent.ParentBuilderImpl();
		}
	}
	public static class Child extends SuperBuilderBasic.Parent {
		double field3;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ChildBuilder<C extends SuperBuilderBasic.Child, B extends SuperBuilderBasic.Child.ChildBuilder<C, B>> extends SuperBuilderBasic.Parent.ParentBuilder<C, B> {
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
				return "SuperBuilderBasic.Child.ChildBuilder(super=" + super.toString() + ", field3=" + this.field3 + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ChildBuilderImpl extends SuperBuilderBasic.Child.ChildBuilder<SuperBuilderBasic.Child, SuperBuilderBasic.Child.ChildBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ChildBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderBasic.Child.ChildBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderBasic.Child build() {
				return new SuperBuilderBasic.Child(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Child(final SuperBuilderBasic.Child.ChildBuilder<?, ?> b) {
			super(b);
			this.field3 = b.field3;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderBasic.Child.ChildBuilder<?, ?> builder() {
			return new SuperBuilderBasic.Child.ChildBuilderImpl();
		}
	}
	public static void test() {
		Child x = Child.builder().field3(0.0).field1(5).item("").build();
	}
}
