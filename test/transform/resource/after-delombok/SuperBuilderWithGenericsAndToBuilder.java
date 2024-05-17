import java.util.Map;
public class SuperBuilderWithGenericsAndToBuilder {
	public static class Parent<A> {
		A field1;
		Map<Integer, String> items;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ParentBuilder<A, C extends SuperBuilderWithGenericsAndToBuilder.Parent<A>, B extends SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilder<A, C, B>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private A field1;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private java.util.ArrayList<Integer> items$key;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private java.util.ArrayList<String> items$value;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected B $fillValuesFrom(final C instance) {
				SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private static <A> void $fillValuesFromInstanceIntoBuilder(final SuperBuilderWithGenericsAndToBuilder.Parent<A> instance, final SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilder<A, ?, ?> b) {
				b.field1(instance.field1);
				b.items(instance.items == null ? java.util.Collections.<Integer, String>emptyMap() : instance.items);
			}
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
			public B item(final Integer itemKey, final String itemValue) {
				if (this.items$key == null) {
					this.items$key = new java.util.ArrayList<Integer>();
					this.items$value = new java.util.ArrayList<String>();
				}
				this.items$key.add(itemKey);
				this.items$value.add(itemValue);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B items(final java.util.Map<? extends Integer, ? extends String> items) {
				if (items == null) {
					throw new java.lang.NullPointerException("items cannot be null");
				}
				if (this.items$key == null) {
					this.items$key = new java.util.ArrayList<Integer>();
					this.items$value = new java.util.ArrayList<String>();
				}
				for (final java.util.Map.Entry<? extends Integer, ? extends String> $lombokEntry : items.entrySet()) {
					this.items$key.add($lombokEntry.getKey());
					this.items$value.add($lombokEntry.getValue());
				}
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B clearItems() {
				if (this.items$key != null) {
					this.items$key.clear();
					this.items$value.clear();
				}
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
				return "SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilder(field1=" + this.field1 + ", items$key=" + this.items$key + ", items$value=" + this.items$value + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ParentBuilderImpl<A> extends SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilder<A, SuperBuilderWithGenericsAndToBuilder.Parent<A>, SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilderImpl<A>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ParentBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilderImpl<A> self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderWithGenericsAndToBuilder.Parent<A> build() {
				return new SuperBuilderWithGenericsAndToBuilder.Parent<A>(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Parent(final SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilder<A, ?, ?> b) {
			this.field1 = b.field1;
			java.util.Map<Integer, String> items;
			switch (b.items$key == null ? 0 : b.items$key.size()) {
			case 0: 
				items = java.util.Collections.emptyMap();
				break;
			case 1: 
				items = java.util.Collections.singletonMap(b.items$key.get(0), b.items$value.get(0));
				break;
			default: 
				items = new java.util.LinkedHashMap<Integer, String>(b.items$key.size() < 1073741824 ? 1 + b.items$key.size() + (b.items$key.size() - 3) / 3 : java.lang.Integer.MAX_VALUE);
				for (int $i = 0; $i < b.items$key.size(); $i++) items.put(b.items$key.get($i), (String) b.items$value.get($i));
				items = java.util.Collections.unmodifiableMap(items);
			}
			this.items = items;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static <A> SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilder<A, ?, ?> builder() {
			return new SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilderImpl<A>();
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilder<A, ?, ?> toBuilder() {
			return new SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilderImpl<A>().$fillValuesFrom(this);
		}
	}
	public static class Child<A> extends Parent<A> {
		double field3;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ChildBuilder<A, C extends SuperBuilderWithGenericsAndToBuilder.Child<A>, B extends SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilder<A, C, B>> extends Parent.ParentBuilder<A, C, B> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private double field3;
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected B $fillValuesFrom(final C instance) {
				super.$fillValuesFrom(instance);
				SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private static <A> void $fillValuesFromInstanceIntoBuilder(final SuperBuilderWithGenericsAndToBuilder.Child<A> instance, final SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilder<A, ?, ?> b) {
				b.field3(instance.field3);
			}
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
				return "SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilder(super=" + super.toString() + ", field3=" + this.field3 + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ChildBuilderImpl<A> extends SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilder<A, SuperBuilderWithGenericsAndToBuilder.Child<A>, SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilderImpl<A>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ChildBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilderImpl<A> self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderWithGenericsAndToBuilder.Child<A> build() {
				return new SuperBuilderWithGenericsAndToBuilder.Child<A>(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Child(final SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilder<A, ?, ?> b) {
			super(b);
			this.field3 = b.field3;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static <A> SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilder<A, ?, ?> builder() {
			return new SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilderImpl<A>();
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilder<A, ?, ?> toBuilder() {
			return new SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilderImpl<A>().$fillValuesFrom(this);
		}
	}
	public static void test() {
		Child<Integer> x = Child.<Integer>builder().field3(0.0).field1(5).item(5, "").build().toBuilder().build();
	}
}
