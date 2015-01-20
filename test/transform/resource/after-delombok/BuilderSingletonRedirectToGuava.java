import java.util.Set;
import java.util.NavigableMap;
import java.util.Collection;
class BuilderSingletonRedirectToGuava {
	private Set<String> dangerMice;
	private NavigableMap<Integer, Number> things;
	private Collection<Class<?>> doohickeys;
	@java.lang.SuppressWarnings("all")
	BuilderSingletonRedirectToGuava(final Set<String> dangerMice, final NavigableMap<Integer, Number> things, final Collection<Class<?>> doohickeys) {
		this.dangerMice = dangerMice;
		this.things = things;
		this.doohickeys = doohickeys;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderSingletonRedirectToGuavaBuilder {
		private com.google.common.collect.ImmutableSet.Builder<String> dangerMice;
		private com.google.common.collect.ImmutableSortedMap.Builder<Integer, Number> things;
		private com.google.common.collect.ImmutableList.Builder<Class<?>> doohickeys;
		@java.lang.SuppressWarnings("all")
		BuilderSingletonRedirectToGuavaBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonRedirectToGuavaBuilder dangerMouse(final String dangerMouse) {
			if (this.dangerMice == null) this.dangerMice = com.google.common.collect.ImmutableSet.builder();
			this.dangerMice.add(dangerMouse);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonRedirectToGuavaBuilder dangerMice(final java.lang.Iterable<? extends String> dangerMice) {
			if (this.dangerMice == null) this.dangerMice = com.google.common.collect.ImmutableSet.builder();
			this.dangerMice.addAll(dangerMice);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonRedirectToGuavaBuilder thing(final Integer thing$key, final Number thing$value) {
			if (this.things == null) this.things = com.google.common.collect.ImmutableSortedMap.naturalOrder();
			this.things.put(thing$key, thing$value);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonRedirectToGuavaBuilder things(final java.util.Map<? extends Integer, ? extends Number> things) {
			if (this.things == null) this.things = com.google.common.collect.ImmutableSortedMap.naturalOrder();
			this.things.putAll(things);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonRedirectToGuavaBuilder doohickey(final Class<?> doohickey) {
			if (this.doohickeys == null) this.doohickeys = com.google.common.collect.ImmutableList.builder();
			this.doohickeys.add(doohickey);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonRedirectToGuavaBuilder doohickeys(final java.lang.Iterable<? extends Class<?>> doohickeys) {
			if (this.doohickeys == null) this.doohickeys = com.google.common.collect.ImmutableList.builder();
			this.doohickeys.addAll(doohickeys);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonRedirectToGuava build() {
			java.util.Set<String> dangerMice = this.dangerMice == null ? com.google.common.collect.ImmutableSet.of() : this.dangerMice.build();
			java.util.NavigableMap<Integer, Number> things = this.things == null ? com.google.common.collect.ImmutableSortedMap.of() : this.things.build();
			java.util.Collection<Class<?>> doohickeys = this.doohickeys == null ? com.google.common.collect.ImmutableList.of() : this.doohickeys.build();
			return new BuilderSingletonRedirectToGuava(dangerMice, things, doohickeys);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderSingletonRedirectToGuava.BuilderSingletonRedirectToGuavaBuilder(dangerMice=" + this.dangerMice + ", things=" + this.things + ", doohickeys=" + this.doohickeys + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderSingletonRedirectToGuavaBuilder builder() {
		return new BuilderSingletonRedirectToGuavaBuilder();
	}
}