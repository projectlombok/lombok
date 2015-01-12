import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
class BuilderSingletonGuavaListsSets<T> {
	private ImmutableList<T> cards;
	private ImmutableCollection<? extends Number> frogs;
	private ImmutableSet rawSet;
	private ImmutableSortedSet<String> passes;
	@java.lang.SuppressWarnings("all")
	BuilderSingletonGuavaListsSets(final ImmutableList<T> cards, final ImmutableCollection<? extends Number> frogs, final ImmutableSet rawSet, final ImmutableSortedSet<String> passes) {
		this.cards = cards;
		this.frogs = frogs;
		this.rawSet = rawSet;
		this.passes = passes;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderSingletonGuavaListsSetsBuilder<T> {
		private com.google.common.collect.ImmutableList.Builder<T> cards;
		private com.google.common.collect.ImmutableList.Builder<Number> frogs;
		private com.google.common.collect.ImmutableSet.Builder<java.lang.Object> rawSet;
		private com.google.common.collect.ImmutableSortedSet.Builder<String> passes;
		@java.lang.SuppressWarnings("all")
		BuilderSingletonGuavaListsSetsBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonGuavaListsSetsBuilder<T> card(final T card) {
			if (this.cards == null) this.cards = com.google.common.collect.ImmutableList.builder();
			this.cards.add(card);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonGuavaListsSetsBuilder<T> cards(final java.lang.Iterable<? extends T> cards) {
			if (this.cards == null) this.cards = com.google.common.collect.ImmutableList.builder();
			this.cards.addAll(cards);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonGuavaListsSetsBuilder<T> frog(final Number frog) {
			if (this.frogs == null) this.frogs = com.google.common.collect.ImmutableList.builder();
			this.frogs.add(frog);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonGuavaListsSetsBuilder<T> frogs(final java.lang.Iterable<? extends Number> frogs) {
			if (this.frogs == null) this.frogs = com.google.common.collect.ImmutableList.builder();
			this.frogs.addAll(frogs);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonGuavaListsSetsBuilder<T> rawSet(final java.lang.Object rawSet) {
			if (this.rawSet == null) this.rawSet = com.google.common.collect.ImmutableSet.builder();
			this.rawSet.add(rawSet);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonGuavaListsSetsBuilder<T> rawSet(final java.lang.Iterable<?> rawSet) {
			if (this.rawSet == null) this.rawSet = com.google.common.collect.ImmutableSet.builder();
			this.rawSet.addAll(rawSet);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonGuavaListsSetsBuilder<T> pass(final String pass) {
			if (this.passes == null) this.passes = com.google.common.collect.ImmutableSortedSet.naturalOrder();
			this.passes.add(pass);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonGuavaListsSetsBuilder<T> passes(final java.lang.Iterable<? extends String> passes) {
			if (this.passes == null) this.passes = com.google.common.collect.ImmutableSortedSet.naturalOrder();
			this.passes.addAll(passes);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonGuavaListsSets<T> build() {
			com.google.common.collect.ImmutableList<T> cards = this.cards == null ? com.google.common.collect.ImmutableList.of() : this.cards.build();
			com.google.common.collect.ImmutableCollection<Number> frogs = this.frogs == null ? com.google.common.collect.ImmutableList.of() : this.frogs.build();
			com.google.common.collect.ImmutableSet<java.lang.Object> rawSet = this.rawSet == null ? com.google.common.collect.ImmutableSet.of() : this.rawSet.build();
			com.google.common.collect.ImmutableSortedSet<String> passes = this.passes == null ? com.google.common.collect.ImmutableSortedSet.of() : this.passes.build();
			return new BuilderSingletonGuavaListsSets<T>(cards, frogs, rawSet, passes);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderSingletonGuavaListsSets.BuilderSingletonGuavaListsSetsBuilder(cards=" + this.cards + ", frogs=" + this.frogs + ", rawSet=" + this.rawSet + ", passes=" + this.passes + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static <T> BuilderSingletonGuavaListsSetsBuilder<T> builder() {
		return new BuilderSingletonGuavaListsSetsBuilder<T>();
	}
}