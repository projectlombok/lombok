//version 8: springframework dep is too new to run on j6
import java.util.List;
class BuilderSingularNullBehavior2 {
	private List<String> locations;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderSingularNullBehavior2(final List<String> locations) {
		this.locations = locations;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderSingularNullBehavior2Builder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private java.util.ArrayList<String> locations;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderSingularNullBehavior2Builder() {
		}
		@org.springframework.lang.NonNull
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder location(final String location) {
			if (this.locations == null) this.locations = new java.util.ArrayList<String>();
			this.locations.add(location);
			return this;
		}
		@org.springframework.lang.NonNull
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder locations(@org.springframework.lang.NonNull final java.util.Collection<? extends String> locations) {
			java.util.Objects.requireNonNull(locations, "locations cannot be null");
			if (this.locations == null) this.locations = new java.util.ArrayList<String>();
			this.locations.addAll(locations);
			return this;
		}
		@org.springframework.lang.NonNull
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder clearLocations() {
			if (this.locations != null) this.locations.clear();
			return this;
		}
		@org.springframework.lang.NonNull
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderSingularNullBehavior2 build() {
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
			return new BuilderSingularNullBehavior2(locations);
		}
		@org.springframework.lang.NonNull
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder(locations=" + this.locations + ")";
		}
	}
	@org.springframework.lang.NonNull
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder builder() {
		return new BuilderSingularNullBehavior2.BuilderSingularNullBehavior2Builder();
	}
}
