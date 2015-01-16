import java.util.List;
import java.util.Collection;
class BuilderSingletonLists<T> {
	private List<T> children;
	private Collection<? extends Number> scarves;
	@SuppressWarnings("all")
	private List rawList;
	@java.lang.SuppressWarnings("all")
	BuilderSingletonLists(final List<T> children, final Collection<? extends Number> scarves, final List rawList) {
		this.children = children;
		this.scarves = scarves;
		this.rawList = rawList;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderSingletonListsBuilder<T> {
		private java.util.ArrayList<T> children;
		private java.util.ArrayList<Number> scarves;
		private java.util.ArrayList<java.lang.Object> rawList;
		@java.lang.SuppressWarnings("all")
		BuilderSingletonListsBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonListsBuilder<T> child(final T child) {
			if (this.children == null) this.children = new java.util.ArrayList<T>();
			this.children.add(child);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonListsBuilder<T> children(final java.util.Collection<? extends T> children) {
			if (this.children == null) this.children = new java.util.ArrayList<T>();
			this.children.addAll(children);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonListsBuilder<T> scarf(final Number scarf) {
			if (this.scarves == null) this.scarves = new java.util.ArrayList<Number>();
			this.scarves.add(scarf);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonListsBuilder<T> scarves(final java.util.Collection<? extends Number> scarves) {
			if (this.scarves == null) this.scarves = new java.util.ArrayList<Number>();
			this.scarves.addAll(scarves);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonListsBuilder<T> rawList(final java.lang.Object rawList) {
			if (this.rawList == null) this.rawList = new java.util.ArrayList<java.lang.Object>();
			this.rawList.add(rawList);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonListsBuilder<T> rawList(final java.util.Collection<?> rawList) {
			if (this.rawList == null) this.rawList = new java.util.ArrayList<java.lang.Object>();
			this.rawList.addAll(rawList);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingletonLists<T> build() {
			java.util.List<T> children;
			switch (this.children == null ? 0 : this.children.size()) {
			case 0: 
				children = java.util.Collections.emptyList();
				break;
			case 1: 
				children = java.util.Collections.singletonList(this.children.get(0));
				break;
			default: 
				children = new java.util.ArrayList<T>(this.children.size());
				children.addAll(this.children);
				children = java.util.Collections.unmodifiableList(children);
			}
			java.util.List<Number> scarves;
			switch (this.scarves == null ? 0 : this.scarves.size()) {
			case 0: 
				scarves = java.util.Collections.emptyList();
				break;
			case 1: 
				scarves = java.util.Collections.singletonList(this.scarves.get(0));
				break;
			default: 
				scarves = new java.util.ArrayList<Number>(this.scarves.size());
				scarves.addAll(this.scarves);
				scarves = java.util.Collections.unmodifiableList(scarves);
			}
			java.util.List<java.lang.Object> rawList;
			switch (this.rawList == null ? 0 : this.rawList.size()) {
			case 0: 
				rawList = java.util.Collections.emptyList();
				break;
			case 1: 
				rawList = java.util.Collections.singletonList(this.rawList.get(0));
				break;
			default: 
				rawList = new java.util.ArrayList<java.lang.Object>(this.rawList.size());
				rawList.addAll(this.rawList);
				rawList = java.util.Collections.unmodifiableList(rawList);
			}
			return new BuilderSingletonLists<T>(children, scarves, rawList);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderSingletonLists.BuilderSingletonListsBuilder(children=" + this.children + ", scarves=" + this.scarves + ", rawList=" + this.rawList + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static <T> BuilderSingletonListsBuilder<T> builder() {
		return new BuilderSingletonListsBuilder<T>();
	}
}
