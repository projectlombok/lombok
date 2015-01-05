import java.util.Set;
import java.util.SortedSet;
class BuilderSingletons<T> {
	private Set<T> dangerMice;
	private SortedSet<? extends Number> octopodes;
	private Set rawSet;
	private Set<String> stringSet;
	@java.lang.SuppressWarnings("all")
	BuilderSingletons(final Set<T> dangerMice, final SortedSet<? extends Number> octopodes, final Set rawSet, final Set<String> stringSet) {
		this.dangerMice = dangerMice;
		this.octopodes = octopodes;
		this.rawSet = rawSet;
		this.stringSet = stringSet;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderSingletonsBuilder<T> {
		private java.util.ArrayList<T> dangerMice;
		private java.util.ArrayList<Number> octopodes;
		private java.util.ArrayList<java.lang.Object> rawSet;
		private java.util.ArrayList<String> stringSet;
		@java.lang.SuppressWarnings("all")
		BuilderSingletonsBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonsBuilder<T> dangerMouse(final T dangerMouse) {
			this.dangerMice.add(dangerMouse);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonsBuilder<T> dangerMice(final java.util.Collection<? extends T> dangerMice) {
			this.dangerMice.addAll(dangerMice);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonsBuilder<T> octopus(final Number octopus) {
			this.octopodes.add(octopus);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonsBuilder<T> octopodes(final java.util.Collection<? extends Number> octopodes) {
			this.octopodes.addAll(octopodes);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonsBuilder<T> rawSet(final java.lang.Object rawSet) {
			this.rawSet.add(rawSet);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonsBuilder<T> rawSet(final java.util.Collection<?> rawSet) {
			this.rawSet.addAll(rawSet);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonsBuilder<T> stringSet(final String stringSet) {
			this.stringSet.add(stringSet);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonsBuilder<T> stringSet(final java.util.Collection<? extends String> stringSet) {
			this.stringSet.addAll(stringSet);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletons<T> build() {
			java.util.Set<T> dangerMice = new java.util.LinkedHashSet<T>(this.dangerMice.size() < 1073741824 ? this.dangerMice.size() < 3 ? this.dangerMice.size() + 1 : this.dangerMice.size() + this.dangerMice.size() / 3 : java.lang.Integer.MAX_VALUE, 0.75F);
			dangerMice.addAll(this.dangerMice);
			dangerMice = java.util.Collections.unmodifiableSet(dangerMice);
			java.util.SortedSet<Number> octopodes = new java.util.TreeSet<Number>();
			octopodes.addAll(this.octopodes);
			octopodes = java.util.Collections.unmodifiableSortedSet(octopodes);
			java.util.Set<java.lang.Object> rawSet = new java.util.LinkedHashSet<java.lang.Object>(this.rawSet.size() < 1073741824 ? this.rawSet.size() < 3 ? this.rawSet.size() + 1 : this.rawSet.size() + this.rawSet.size() / 3 : java.lang.Integer.MAX_VALUE, 0.75F);
			rawSet.addAll(this.rawSet);
			rawSet = java.util.Collections.unmodifiableSet(rawSet);
			java.util.Set<String> stringSet = new java.util.LinkedHashSet<String>(this.stringSet.size() < 1073741824 ? this.stringSet.size() < 3 ? this.stringSet.size() + 1 : this.stringSet.size() + this.stringSet.size() / 3 : java.lang.Integer.MAX_VALUE, 0.75F);
			stringSet.addAll(this.stringSet);
			stringSet = java.util.Collections.unmodifiableSet(stringSet);
			return new BuilderSingletons<T>(dangerMice, octopodes, rawSet, stringSet);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderSingletons.BuilderSingletonsBuilder(dangerMice=" + this.dangerMice + ", octopodes=" + this.octopodes + ", rawSet=" + this.rawSet + ", stringSet=" + this.stringSet + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static <T> BuilderSingletonsBuilder<T> builder() {
		return new BuilderSingletonsBuilder<T>();
	}
}