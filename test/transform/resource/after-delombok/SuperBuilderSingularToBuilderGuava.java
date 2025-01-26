public class SuperBuilderSingularToBuilderGuava {
	public static class Parent<T> {
		private com.google.common.collect.ImmutableList<T> cards;
		private com.google.common.collect.ImmutableCollection<? extends Number> frogs;
		@SuppressWarnings("all")
		private com.google.common.collect.ImmutableSet rawSet;
		private com.google.common.collect.ImmutableSortedSet<String> passes;
		private com.google.common.collect.ImmutableTable<? extends Number, ? extends Number, String> users;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ParentBuilder<T, C extends SuperBuilderSingularToBuilderGuava.Parent<T>, B extends SuperBuilderSingularToBuilderGuava.Parent.ParentBuilder<T, C, B>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private com.google.common.collect.ImmutableList.Builder<T> cards;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private com.google.common.collect.ImmutableList.Builder<Number> frogs;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private com.google.common.collect.ImmutableSet.Builder<java.lang.Object> rawSet;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private com.google.common.collect.ImmutableSortedSet.Builder<String> passes;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private com.google.common.collect.ImmutableTable.Builder<Number, Number, String> users;
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected B $fillValuesFrom(final C instance) {
				SuperBuilderSingularToBuilderGuava.Parent.ParentBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private static <T> void $fillValuesFromInstanceIntoBuilder(final SuperBuilderSingularToBuilderGuava.Parent<T> instance, final SuperBuilderSingularToBuilderGuava.Parent.ParentBuilder<T, ?, ?> b) {
				b.cards(instance.cards == null ? com.google.common.collect.ImmutableList.<T>of() : instance.cards);
				b.frogs(instance.frogs == null ? com.google.common.collect.ImmutableList.<Number>of() : instance.frogs);
				b.rawSet(instance.rawSet == null ? com.google.common.collect.ImmutableSet.<java.lang.Object>of() : instance.rawSet);
				b.passes(instance.passes == null ? com.google.common.collect.ImmutableSortedSet.<String>of() : instance.passes);
				b.users(instance.users == null ? com.google.common.collect.ImmutableTable.<Number, Number, String>of() : instance.users);
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B card(final T card) {
				if (this.cards == null) this.cards = com.google.common.collect.ImmutableList.builder();
				this.cards.add(card);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B cards(final java.lang.Iterable<? extends T> cards) {
				if (cards == null) {
					throw new java.lang.NullPointerException("cards cannot be null");
				}
				if (this.cards == null) this.cards = com.google.common.collect.ImmutableList.builder();
				this.cards.addAll(cards);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B clearCards() {
				this.cards = null;
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B frog(final Number frog) {
				if (this.frogs == null) this.frogs = com.google.common.collect.ImmutableList.builder();
				this.frogs.add(frog);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B frogs(final java.lang.Iterable<? extends Number> frogs) {
				if (frogs == null) {
					throw new java.lang.NullPointerException("frogs cannot be null");
				}
				if (this.frogs == null) this.frogs = com.google.common.collect.ImmutableList.builder();
				this.frogs.addAll(frogs);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B clearFrogs() {
				this.frogs = null;
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B rawSet(final java.lang.Object rawSet) {
				if (this.rawSet == null) this.rawSet = com.google.common.collect.ImmutableSet.builder();
				this.rawSet.add(rawSet);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B rawSet(final java.lang.Iterable<?> rawSet) {
				if (rawSet == null) {
					throw new java.lang.NullPointerException("rawSet cannot be null");
				}
				if (this.rawSet == null) this.rawSet = com.google.common.collect.ImmutableSet.builder();
				this.rawSet.addAll(rawSet);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B clearRawSet() {
				this.rawSet = null;
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B pass(final String pass) {
				if (this.passes == null) this.passes = com.google.common.collect.ImmutableSortedSet.naturalOrder();
				this.passes.add(pass);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B passes(final java.lang.Iterable<? extends String> passes) {
				if (passes == null) {
					throw new java.lang.NullPointerException("passes cannot be null");
				}
				if (this.passes == null) this.passes = com.google.common.collect.ImmutableSortedSet.naturalOrder();
				this.passes.addAll(passes);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B clearPasses() {
				this.passes = null;
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B user(final Number rowKey, final Number columnKey, final String value) {
				if (this.users == null) this.users = com.google.common.collect.ImmutableTable.builder();
				this.users.put(rowKey, columnKey, value);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B users(final com.google.common.collect.Table<? extends Number, ? extends Number, ? extends String> users) {
				if (users == null) {
					throw new java.lang.NullPointerException("users cannot be null");
				}
				if (this.users == null) this.users = com.google.common.collect.ImmutableTable.builder();
				this.users.putAll(users);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public B clearUsers() {
				this.users = null;
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
				return "SuperBuilderSingularToBuilderGuava.Parent.ParentBuilder(cards=" + this.cards + ", frogs=" + this.frogs + ", rawSet=" + this.rawSet + ", passes=" + this.passes + ", users=" + this.users + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ParentBuilderImpl<T> extends SuperBuilderSingularToBuilderGuava.Parent.ParentBuilder<T, SuperBuilderSingularToBuilderGuava.Parent<T>, SuperBuilderSingularToBuilderGuava.Parent.ParentBuilderImpl<T>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ParentBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderSingularToBuilderGuava.Parent.ParentBuilderImpl<T> self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderSingularToBuilderGuava.Parent<T> build() {
				return new SuperBuilderSingularToBuilderGuava.Parent<T>(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Parent(final SuperBuilderSingularToBuilderGuava.Parent.ParentBuilder<T, ?, ?> b) {
			com.google.common.collect.ImmutableList<T> cards = b.cards == null ? com.google.common.collect.ImmutableList.<T>of() : b.cards.build();
			this.cards = cards;
			com.google.common.collect.ImmutableCollection<Number> frogs = b.frogs == null ? com.google.common.collect.ImmutableList.<Number>of() : b.frogs.build();
			this.frogs = frogs;
			com.google.common.collect.ImmutableSet<java.lang.Object> rawSet = b.rawSet == null ? com.google.common.collect.ImmutableSet.<java.lang.Object>of() : b.rawSet.build();
			this.rawSet = rawSet;
			com.google.common.collect.ImmutableSortedSet<String> passes = b.passes == null ? com.google.common.collect.ImmutableSortedSet.<String>of() : b.passes.build();
			this.passes = passes;
			com.google.common.collect.ImmutableTable<Number, Number, String> users = b.users == null ? com.google.common.collect.ImmutableTable.<Number, Number, String>of() : b.users.build();
			this.users = users;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static <T> SuperBuilderSingularToBuilderGuava.Parent.ParentBuilder<T, ?, ?> builder() {
			return new SuperBuilderSingularToBuilderGuava.Parent.ParentBuilderImpl<T>();
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public SuperBuilderSingularToBuilderGuava.Parent.ParentBuilder<T, ?, ?> toBuilder() {
			return new SuperBuilderSingularToBuilderGuava.Parent.ParentBuilderImpl<T>().$fillValuesFrom(this);
		}
	}
	public static class Child<T> extends Parent<T> {
		private double field3;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static abstract class ChildBuilder<T, C extends SuperBuilderSingularToBuilderGuava.Child<T>, B extends SuperBuilderSingularToBuilderGuava.Child.ChildBuilder<T, C, B>> extends Parent.ParentBuilder<T, C, B> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private double field3;
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected B $fillValuesFrom(final C instance) {
				super.$fillValuesFrom(instance);
				SuperBuilderSingularToBuilderGuava.Child.ChildBuilder.$fillValuesFromInstanceIntoBuilder(instance, this);
				return self();
			}
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private static <T> void $fillValuesFromInstanceIntoBuilder(final SuperBuilderSingularToBuilderGuava.Child<T> instance, final SuperBuilderSingularToBuilderGuava.Child.ChildBuilder<T, ?, ?> b) {
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
				return "SuperBuilderSingularToBuilderGuava.Child.ChildBuilder(super=" + super.toString() + ", field3=" + this.field3 + ")";
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private static final class ChildBuilderImpl<T> extends SuperBuilderSingularToBuilderGuava.Child.ChildBuilder<T, SuperBuilderSingularToBuilderGuava.Child<T>, SuperBuilderSingularToBuilderGuava.Child.ChildBuilderImpl<T>> {
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			private ChildBuilderImpl() {
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			protected SuperBuilderSingularToBuilderGuava.Child.ChildBuilderImpl<T> self() {
				return this;
			}
			@java.lang.Override
			@java.lang.SuppressWarnings("all")
			@lombok.Generated
			public SuperBuilderSingularToBuilderGuava.Child<T> build() {
				return new SuperBuilderSingularToBuilderGuava.Child<T>(this);
			}
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		protected Child(final SuperBuilderSingularToBuilderGuava.Child.ChildBuilder<T, ?, ?> b) {
			super(b);
			this.field3 = b.field3;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public static <T> SuperBuilderSingularToBuilderGuava.Child.ChildBuilder<T, ?, ?> builder() {
			return new SuperBuilderSingularToBuilderGuava.Child.ChildBuilderImpl<T>();
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public SuperBuilderSingularToBuilderGuava.Child.ChildBuilder<T, ?, ?> toBuilder() {
			return new SuperBuilderSingularToBuilderGuava.Child.ChildBuilderImpl<T>().$fillValuesFrom(this);
		}
	}
	public static void test() {
		Child<Integer> x = Child.<Integer>builder().card(1).build().toBuilder().build();
	}
}
