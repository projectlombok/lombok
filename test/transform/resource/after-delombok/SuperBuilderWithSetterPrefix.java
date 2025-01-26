import java.util.List;
public class SuperBuilderWithSetterPrefix {
	public static class Parent {
		private int field1;
		int obtainViaField;
		int obtainViaMethod;
		String obtainViaStaticMethod;
		List<String> items;
		private int method() {
			return 2;
		}
		private static String staticMethod(Parent instance) {
			return "staticMethod";
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ParentBuilder<C extends SuperBuilderWithSetterPrefix.Parent, B extends SuperBuilderWithSetterPrefix.Parent.ParentBuilder<C, B>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private int field1;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private int obtainViaField;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private int obtainViaMethod;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private String obtainViaStaticMethod;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private java.util.ArrayList<String> items;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected B $fillValuesFrom(final C instance) {
				SuperBuilderWithSetterPrefix.Parent.ParentBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private static void $fillValuesFromInstanceIntoBuilder(final SuperBuilderWithSetterPrefix.Parent instance, final SuperBuilderWithSetterPrefix.Parent.ParentBuilder<?, ?> b) {
				b.withField1(instance.field1);
				b.withObtainViaField(instance.field1);
				b.withObtainViaMethod(instance.method());
				b.withObtainViaStaticMethod(SuperBuilderWithSetterPrefix.Parent.staticMethod(instance));
				b.withItems(instance.items == null ? java.util.Collections.<String>emptyList() : instance.items);
			}
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B withField1(final int field1) {
				this.field1 = field1;
				return self();
			}
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B withObtainViaField(final int obtainViaField) {
				this.obtainViaField = obtainViaField;
				return self();
			}
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B withObtainViaMethod(final int obtainViaMethod) {
				this.obtainViaMethod = obtainViaMethod;
				return self();
			}
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B withObtainViaStaticMethod(final String obtainViaStaticMethod) {
				this.obtainViaStaticMethod = obtainViaStaticMethod;
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B withItem(final String item) {
				if (this.items == null) this.items = new java.util.ArrayList<String>();
				this.items.add(item);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B withItems(final java.util.Collection<? extends String> items) {
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
				return "SuperBuilderWithSetterPrefix.Parent.ParentBuilder(field1=" + this.field1 + ", obtainViaField=" + this.obtainViaField + ", obtainViaMethod=" + this.obtainViaMethod + ", obtainViaStaticMethod=" + this.obtainViaStaticMethod + ", items=" + this.items + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ParentBuilderImpl extends SuperBuilderWithSetterPrefix.Parent.ParentBuilder<SuperBuilderWithSetterPrefix.Parent, SuperBuilderWithSetterPrefix.Parent.ParentBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ParentBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderWithSetterPrefix.Parent.ParentBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderWithSetterPrefix.Parent build() {
				return new SuperBuilderWithSetterPrefix.Parent(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Parent(final SuperBuilderWithSetterPrefix.Parent.ParentBuilder<?, ?> b) {
			this.field1 = b.field1;
			this.obtainViaField = b.obtainViaField;
			this.obtainViaMethod = b.obtainViaMethod;
			this.obtainViaStaticMethod = b.obtainViaStaticMethod;
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
		public static SuperBuilderWithSetterPrefix.Parent.ParentBuilder<?, ?> builder() {
			return new SuperBuilderWithSetterPrefix.Parent.ParentBuilderImpl();
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public SuperBuilderWithSetterPrefix.Parent.ParentBuilder<?, ?> toBuilder() {
			return new SuperBuilderWithSetterPrefix.Parent.ParentBuilderImpl().$fillValuesFrom(this);
		}
	}
	public static class Child extends Parent {
		private double field3;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ChildBuilder<C extends SuperBuilderWithSetterPrefix.Child, B extends SuperBuilderWithSetterPrefix.Child.ChildBuilder<C, B>> extends Parent.ParentBuilder<C, B> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private double field3;
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected B $fillValuesFrom(final C instance) {
				super.$fillValuesFrom(instance);
				SuperBuilderWithSetterPrefix.Child.ChildBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private static void $fillValuesFromInstanceIntoBuilder(final SuperBuilderWithSetterPrefix.Child instance, final SuperBuilderWithSetterPrefix.Child.ChildBuilder<?, ?> b) {
				b.setField3(instance.field3);
			}
			/**
			 * @return {@code this}.
			 */
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B setField3(final double field3) {
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
				return "SuperBuilderWithSetterPrefix.Child.ChildBuilder(super=" + super.toString() + ", field3=" + this.field3 + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ChildBuilderImpl extends SuperBuilderWithSetterPrefix.Child.ChildBuilder<SuperBuilderWithSetterPrefix.Child, SuperBuilderWithSetterPrefix.Child.ChildBuilderImpl> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ChildBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderWithSetterPrefix.Child.ChildBuilderImpl self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderWithSetterPrefix.Child build() {
				return new SuperBuilderWithSetterPrefix.Child(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Child(final SuperBuilderWithSetterPrefix.Child.ChildBuilder<?, ?> b) {
			super(b);
			this.field3 = b.field3;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static SuperBuilderWithSetterPrefix.Child.ChildBuilder<?, ?> builder() {
			return new SuperBuilderWithSetterPrefix.Child.ChildBuilderImpl();
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public SuperBuilderWithSetterPrefix.Child.ChildBuilder<?, ?> toBuilder() {
			return new SuperBuilderWithSetterPrefix.Child.ChildBuilderImpl().$fillValuesFrom(this);
		}
	}
	public static void test() {
		Child x = Child.builder().setField3(0.0).withField1(5).withItem("").build().toBuilder().build();
	}
}
