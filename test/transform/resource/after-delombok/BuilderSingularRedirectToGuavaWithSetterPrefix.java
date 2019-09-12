import java.util.Set;
import java.util.NavigableMap;
import java.util.Collection;
class BuilderSingularRedirectToGuavaWithSetterPrefix {
	private Set<String> dangerMice;
	private NavigableMap<Integer, Number> things;
	private Collection<Class<?>> doohickeys;
	@java.lang.SuppressWarnings("all")
	BuilderSingularRedirectToGuavaWithSetterPrefix(final Set<String> dangerMice, final NavigableMap<Integer, Number> things, final Collection<Class<?>> doohickeys) {
		this.dangerMice = dangerMice;
		this.things = things;
		this.doohickeys = doohickeys;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderSingularRedirectToGuavaWithSetterPrefixBuilder {
		@java.lang.SuppressWarnings("all")
		private com.google.common.collect.ImmutableSet.Builder<String> dangerMice;
		@java.lang.SuppressWarnings("all")
		private com.google.common.collect.ImmutableSortedMap.Builder<Integer, Number> things;
		@java.lang.SuppressWarnings("all")
		private com.google.common.collect.ImmutableList.Builder<Class<?>> doohickeys;
		@java.lang.SuppressWarnings("all")
		BuilderSingularRedirectToGuavaWithSetterPrefixBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularRedirectToGuavaWithSetterPrefixBuilder withDangerMouse(final String dangerMouse) {
			if (this.dangerMice == null) this.dangerMice = com.google.common.collect.ImmutableSet.builder();
			this.dangerMice.add(dangerMouse);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularRedirectToGuavaWithSetterPrefixBuilder withDangerMice(final java.lang.Iterable<? extends String> dangerMice) {
			if (this.dangerMice == null) this.dangerMice = com.google.common.collect.ImmutableSet.builder();
			this.dangerMice.addAll(dangerMice);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularRedirectToGuavaWithSetterPrefixBuilder clearDangerMice() {
			this.dangerMice = null;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularRedirectToGuavaWithSetterPrefixBuilder withThing(final Integer key, final Number value) {
			if (this.things == null) this.things = com.google.common.collect.ImmutableSortedMap.naturalOrder();
			this.things.put(key, value);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularRedirectToGuavaWithSetterPrefixBuilder withThings(final java.util.Map<? extends Integer, ? extends Number> things) {
			if (this.things == null) this.things = com.google.common.collect.ImmutableSortedMap.naturalOrder();
			this.things.putAll(things);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularRedirectToGuavaWithSetterPrefixBuilder clearThings() {
			this.things = null;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularRedirectToGuavaWithSetterPrefixBuilder withDoohickey(final Class<?> doohickey) {
			if (this.doohickeys == null) this.doohickeys = com.google.common.collect.ImmutableList.builder();
			this.doohickeys.add(doohickey);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularRedirectToGuavaWithSetterPrefixBuilder withDoohickeys(final java.lang.Iterable<? extends Class<?>> doohickeys) {
			if (this.doohickeys == null) this.doohickeys = com.google.common.collect.ImmutableList.builder();
			this.doohickeys.addAll(doohickeys);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularRedirectToGuavaWithSetterPrefixBuilder clearDoohickeys() {
			this.doohickeys = null;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularRedirectToGuavaWithSetterPrefix build() {
			java.util.Set<String> dangerMice = this.dangerMice == null ? com.google.common.collect.ImmutableSet.<String>of() : this.dangerMice.build();
			java.util.NavigableMap<Integer, Number> things = this.things == null ? com.google.common.collect.ImmutableSortedMap.<Integer, Number>of() : this.things.build();
			java.util.Collection<Class<?>> doohickeys = this.doohickeys == null ? com.google.common.collect.ImmutableList.<Class<?>>of() : this.doohickeys.build();
			return new BuilderSingularRedirectToGuavaWithSetterPrefix(dangerMice, things, doohickeys);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderSingularRedirectToGuavaWithSetterPrefix.BuilderSingularRedirectToGuavaWithSetterPrefixBuilder(dangerMice=" + this.dangerMice + ", things=" + this.things + ", doohickeys=" + this.doohickeys + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderSingularRedirectToGuavaWithSetterPrefixBuilder builder() {
		return new BuilderSingularRedirectToGuavaWithSetterPrefixBuilder();
	}
}
