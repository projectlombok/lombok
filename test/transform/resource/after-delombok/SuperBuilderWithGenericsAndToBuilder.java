import java.util.Map;
public class SuperBuilderWithGenericsAndToBuilder {
	public static class Parent<A> {
		A field1;
		Map<Integer, String> items;
		@java.lang.SuppressWarnings("all")
		public static abstract class ParentBuilder<A, C extends Parent<A>, B extends ParentBuilder<A, C, B>> {
			@java.lang.SuppressWarnings("all")
			private A field1;
			@java.lang.SuppressWarnings("all")
			private java.util.ArrayList<Integer> items$key;
			@java.lang.SuppressWarnings("all")
			private java.util.ArrayList<String> items$value;
			@java.lang.SuppressWarnings("all")
			protected B $fillValuesFrom(final C instance) {
				ParentBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			private static <A> void $fillValuesFromInstanceIntoBuilder(final Parent<A> instance, final ParentBuilder<A, ?, ?> b) {
				b.field1(instance.field1);
				b.items(instance.items == null ? java.util.Collections.emptyMap() : instance.items);
			}
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
			public B items(final java.util.Map<? extends Integer, ? extends String> items) {
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
			public B clearItems() {
				if (this.items$key != null) {
					this.items$key.clear();
					this.items$value.clear();
				}
				return self();
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public java.lang.String toString() {
				return "SuperBuilderWithGenericsAndToBuilder.Parent.ParentBuilder(field1=" + this.field1 + ", items$key=" + this.items$key + ", items$value=" + this.items$value + ")";
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
		public static <A> ParentBuilder<A, ?, ?> builder() {
			return new ParentBuilderImpl<A>();
		}
		@java.lang.SuppressWarnings("all")
		public ParentBuilder<A, ?, ?> toBuilder() {
			return new ParentBuilderImpl<A>().$fillValuesFrom(this);
		}
	}
	public static class Child<A> extends Parent<A> {
		double field3;
		@java.lang.SuppressWarnings("all")
		public static abstract class ChildBuilder<A, C extends Child<A>, B extends ChildBuilder<A, C, B>> extends Parent.ParentBuilder<A, C, B> {
			@java.lang.SuppressWarnings("all")
			private double field3;
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected B $fillValuesFrom(final C instance) {
				super.$fillValuesFrom(instance);
				ChildBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			private static <A> void $fillValuesFromInstanceIntoBuilder(final Child<A> instance, final ChildBuilder<A, ?, ?> b) {
				b.field3(instance.field3);
			}
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
				return "SuperBuilderWithGenericsAndToBuilder.Child.ChildBuilder(super=" + super.toString() + ", field3=" + this.field3 + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		private static final class ChildBuilderImpl<A> extends ChildBuilder<A, Child<A>, ChildBuilderImpl<A>> {
			@java.lang.SuppressWarnings("all")
			private ChildBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			protected ChildBuilderImpl<A> self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			public Child<A> build() {
				return new Child<A>(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		protected Child(final ChildBuilder<A, ?, ?> b) {
			super(b);
			this.field3 = b.field3;
		}
		@java.lang.SuppressWarnings("all")
		public static <A> ChildBuilder<A, ?, ?> builder() {
			return new ChildBuilderImpl<A>();
		}
		@java.lang.SuppressWarnings("all")
		public ChildBuilder<A, ?, ?> toBuilder() {
			return new ChildBuilderImpl<A>().$fillValuesFrom(this);
		}
	}
	public static void test() {
		Child<Integer> x = Child.<Integer>builder().field3(0.0).field1(5).item(5, "").build().toBuilder().build();
	}
}
