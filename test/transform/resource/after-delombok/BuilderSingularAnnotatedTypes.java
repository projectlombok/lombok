import java.util.Set;
import java.util.Map;
import lombok.NonNull;
class BuilderSingularAnnotatedTypes {
	private Set<@NonNull String> foos;
	private Map<@NonNull String, @NonNull Integer> bars;
	@java.lang.SuppressWarnings("all")
	BuilderSingularAnnotatedTypes(final Set<@NonNull String> foos, final Map<@NonNull String, @NonNull Integer> bars) {
		this.foos = foos;
		this.bars = bars;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderSingularAnnotatedTypesBuilder {
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<@NonNull String> foos;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<@NonNull String> bars$key;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<@NonNull Integer> bars$value;
		@java.lang.SuppressWarnings("all")
		BuilderSingularAnnotatedTypesBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularAnnotatedTypesBuilder foo(final @NonNull String foo) {
			if (this.foos == null) this.foos = new java.util.ArrayList<@NonNull String>();
			this.foos.add(foo);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularAnnotatedTypesBuilder foos(final java.util.Collection<? extends @NonNull String> foos) {
			if (this.foos == null) this.foos = new java.util.ArrayList<@NonNull String>();
			this.foos.addAll(foos);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularAnnotatedTypesBuilder clearFoos() {
			if (this.foos != null) this.foos.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularAnnotatedTypesBuilder bar(final @NonNull String barKey, final @NonNull Integer barValue) {
			if (this.bars$key == null) {
				this.bars$key = new java.util.ArrayList<@NonNull String>();
				this.bars$value = new java.util.ArrayList<@NonNull Integer>();
			}
			this.bars$key.add(barKey);
			this.bars$value.add(barValue);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularAnnotatedTypesBuilder bars(final java.util.Map<? extends @NonNull String, ? extends @NonNull Integer> bars) {
			if (this.bars$key == null) {
				this.bars$key = new java.util.ArrayList<@NonNull String>();
				this.bars$value = new java.util.ArrayList<@NonNull Integer>();
			}
			for (final java.util.Map.Entry<? extends @NonNull String, ? extends @NonNull Integer> $lombokEntry : bars.entrySet()) {
				this.bars$key.add($lombokEntry.getKey());
				this.bars$value.add($lombokEntry.getValue());
			}
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularAnnotatedTypesBuilder clearBars() {
			if (this.bars$key != null) {
				this.bars$key.clear();
				this.bars$value.clear();
			}
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularAnnotatedTypes build() {
			java.util.Set<@NonNull String> foos;
			switch (this.foos == null ? 0 : this.foos.size()) {
			case 0: 
				foos = java.util.Collections.emptySet();
				break;
			case 1: 
				foos = java.util.Collections.singleton(this.foos.get(0));
				break;
			default: 
				foos = new java.util.LinkedHashSet<@NonNull String>(this.foos.size() < 1073741824 ? 1 + this.foos.size() + (this.foos.size() - 3) / 3 : java.lang.Integer.MAX_VALUE);
				foos.addAll(this.foos);
				foos = java.util.Collections.unmodifiableSet(foos);
			}
			java.util.Map<@NonNull String, @NonNull Integer> bars;
			switch (this.bars$key == null ? 0 : this.bars$key.size()) {
			case 0: 
				bars = java.util.Collections.emptyMap();
				break;
			case 1: 
				bars = java.util.Collections.singletonMap(this.bars$key.get(0), this.bars$value.get(0));
				break;
			default: 
				bars = new java.util.LinkedHashMap<@NonNull String, @NonNull Integer>(this.bars$key.size() < 1073741824 ? 1 + this.bars$key.size() + (this.bars$key.size() - 3) / 3 : java.lang.Integer.MAX_VALUE);
				for (int $i = 0; $i < this.bars$key.size(); $i++) bars.put(this.bars$key.get($i), (@NonNull Integer) this.bars$value.get($i));
				bars = java.util.Collections.unmodifiableMap(bars);
			}
			return new BuilderSingularAnnotatedTypes(foos, bars);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderSingularAnnotatedTypes.BuilderSingularAnnotatedTypesBuilder(foos=" + this.foos + ", bars$key=" + this.bars$key + ", bars$value=" + this.bars$value + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderSingularAnnotatedTypesBuilder builder() {
		return new BuilderSingularAnnotatedTypesBuilder();
	}
}