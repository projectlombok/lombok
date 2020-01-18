import java.util.List;
import java.util.Collection;
import lombok.Singular.NullCollectionBehavior;
class BuilderSingularNullBehavior1 {
	private List<String> names;
	private List<String> locations;
	private List<String> whatevers;
	private List<String> doohickeys;
	@java.lang.SuppressWarnings("all")
	BuilderSingularNullBehavior1(final List<String> names, final List<String> locations, final List<String> whatevers, final List<String> doohickeys) {
		this.names = names;
		this.locations = locations;
		this.whatevers = whatevers;
		this.doohickeys = doohickeys;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderSingularNullBehavior1Builder {
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<String> names;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<String> locations;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<String> whatevers;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<String> doohickeys;
		@java.lang.SuppressWarnings("all")
		BuilderSingularNullBehavior1Builder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNullBehavior1.BuilderSingularNullBehavior1Builder name(final String name) {
			if (this.names == null) this.names = new java.util.ArrayList<String>();
			this.names.add(name);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNullBehavior1.BuilderSingularNullBehavior1Builder names(final java.util.Collection<? extends String> names) {
			if (names == null) throw new java.lang.NullPointerException("names cannot be null");
			if (this.names == null) this.names = new java.util.ArrayList<String>();
			this.names.addAll(names);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNullBehavior1.BuilderSingularNullBehavior1Builder clearNames() {
			if (this.names != null) this.names.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNullBehavior1.BuilderSingularNullBehavior1Builder location(final String location) {
			if (this.locations == null) this.locations = new java.util.ArrayList<String>();
			this.locations.add(location);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNullBehavior1.BuilderSingularNullBehavior1Builder locations(final java.util.Collection<? extends String> locations) {
			java.util.Objects.requireNonNull(locations, "locations cannot be null");
			if (this.locations == null) this.locations = new java.util.ArrayList<String>();
			this.locations.addAll(locations);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNullBehavior1.BuilderSingularNullBehavior1Builder clearLocations() {
			if (this.locations != null) this.locations.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNullBehavior1.BuilderSingularNullBehavior1Builder whatever(final String whatever) {
			if (this.whatevers == null) this.whatevers = new java.util.ArrayList<String>();
			this.whatevers.add(whatever);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNullBehavior1.BuilderSingularNullBehavior1Builder whatevers(final java.util.Collection<? extends String> whatevers) {
			com.google.common.base.Preconditions.checkNotNull(whatevers, "whatevers cannot be null");
			if (this.whatevers == null) this.whatevers = new java.util.ArrayList<String>();
			this.whatevers.addAll(whatevers);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNullBehavior1.BuilderSingularNullBehavior1Builder clearWhatevers() {
			if (this.whatevers != null) this.whatevers.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNullBehavior1.BuilderSingularNullBehavior1Builder doohickey(final String doohickey) {
			if (this.doohickeys == null) this.doohickeys = new java.util.ArrayList<String>();
			this.doohickeys.add(doohickey);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNullBehavior1.BuilderSingularNullBehavior1Builder doohickeys(final java.util.Collection<? extends String> doohickeys) {
			if (doohickeys != null) {
				if (this.doohickeys == null) this.doohickeys = new java.util.ArrayList<String>();
				this.doohickeys.addAll(doohickeys);
			}
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNullBehavior1.BuilderSingularNullBehavior1Builder clearDoohickeys() {
			if (this.doohickeys != null) this.doohickeys.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularNullBehavior1 build() {
			java.util.List<String> names;
			switch (this.names == null ? 0 : this.names.size()) {
			case 0: 
				names = java.util.Collections.emptyList();
				break;
			case 1: 
				names = java.util.Collections.singletonList(this.names.get(0));
				break;
			default: 
				names = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.names));
			}
			java.util.List<String> locations;
			switch (this.locations == null ? 0 : this.locations.size()) {
			case 0: 
				locations = java.util.Collections.emptyList();
				break;
			case 1: 
				locations = java.util.Collections.singletonList(this.locations.get(0));
				break;
			default: 
				locations = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.locations));
			}
			java.util.List<String> whatevers;
			switch (this.whatevers == null ? 0 : this.whatevers.size()) {
			case 0: 
				whatevers = java.util.Collections.emptyList();
				break;
			case 1: 
				whatevers = java.util.Collections.singletonList(this.whatevers.get(0));
				break;
			default: 
				whatevers = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.whatevers));
			}
			java.util.List<String> doohickeys;
			switch (this.doohickeys == null ? 0 : this.doohickeys.size()) {
			case 0: 
				doohickeys = java.util.Collections.emptyList();
				break;
			case 1: 
				doohickeys = java.util.Collections.singletonList(this.doohickeys.get(0));
				break;
			default: 
				doohickeys = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.doohickeys));
			}
			return new BuilderSingularNullBehavior1(names, locations, whatevers, doohickeys);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderSingularNullBehavior1.BuilderSingularNullBehavior1Builder(names=" + this.names + ", locations=" + this.locations + ", whatevers=" + this.whatevers + ", doohickeys=" + this.doohickeys + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderSingularNullBehavior1.BuilderSingularNullBehavior1Builder builder() {
		return new BuilderSingularNullBehavior1.BuilderSingularNullBehavior1Builder();
	}
}