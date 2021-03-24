// version 14:
import java.util.Collection;
import java.util.List;
public record BuilderSingularOnRecord<T>(List<T> children, Collection<? extends Number> scarves, @SuppressWarnings("all") List rawList) {
	@java.lang.SuppressWarnings("all")
	public static class BuilderSingularOnRecordBuilder<T> {
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<T> children;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<Number> scarves;
		@java.lang.SuppressWarnings("all")
		private java.util.ArrayList<java.lang.Object> rawList;
		@java.lang.SuppressWarnings("all")
		BuilderSingularOnRecordBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularOnRecord.BuilderSingularOnRecordBuilder<T> child(final T child) {
			if (this.children == null) this.children = new java.util.ArrayList<T>();
			this.children.add(child);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularOnRecord.BuilderSingularOnRecordBuilder<T> children(final java.util.Collection<? extends T> children) {
			if (children == null) {
				throw new java.lang.NullPointerException("children cannot be null");
			}
			if (this.children == null) this.children = new java.util.ArrayList<T>();
			this.children.addAll(children);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularOnRecord.BuilderSingularOnRecordBuilder<T> clearChildren() {
			if (this.children != null) this.children.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularOnRecord.BuilderSingularOnRecordBuilder<T> scarf(final Number scarf) {
			if (this.scarves == null) this.scarves = new java.util.ArrayList<Number>();
			this.scarves.add(scarf);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularOnRecord.BuilderSingularOnRecordBuilder<T> scarves(final java.util.Collection<? extends Number> scarves) {
			if (scarves == null) {
				throw new java.lang.NullPointerException("scarves cannot be null");
			}
			if (this.scarves == null) this.scarves = new java.util.ArrayList<Number>();
			this.scarves.addAll(scarves);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularOnRecord.BuilderSingularOnRecordBuilder<T> clearScarves() {
			if (this.scarves != null) this.scarves.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularOnRecord.BuilderSingularOnRecordBuilder<T> rawList(final java.lang.Object rawList) {
			if (this.rawList == null) this.rawList = new java.util.ArrayList<java.lang.Object>();
			this.rawList.add(rawList);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularOnRecord.BuilderSingularOnRecordBuilder<T> rawList(final java.util.Collection<?> rawList) {
			if (rawList == null) {
				throw new java.lang.NullPointerException("rawList cannot be null");
			}
			if (this.rawList == null) this.rawList = new java.util.ArrayList<java.lang.Object>();
			this.rawList.addAll(rawList);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularOnRecord.BuilderSingularOnRecordBuilder<T> clearRawList() {
			if (this.rawList != null) this.rawList.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderSingularOnRecord<T> build() {
			java.util.List<T> children;
			switch (this.children == null ? 0 : this.children.size()) {
			case 0: 
				children = java.util.Collections.emptyList();
				break;
			case 1: 
				children = java.util.Collections.singletonList(this.children.get(0));
				break;
			default: 
				children = java.util.Collections.unmodifiableList(new java.util.ArrayList<T>(this.children));
			}
			java.util.Collection<Number> scarves;
			switch (this.scarves == null ? 0 : this.scarves.size()) {
			case 0: 
				scarves = java.util.Collections.emptyList();
				break;
			case 1: 
				scarves = java.util.Collections.singletonList(this.scarves.get(0));
				break;
			default: 
				scarves = java.util.Collections.unmodifiableList(new java.util.ArrayList<Number>(this.scarves));
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
				rawList = java.util.Collections.unmodifiableList(new java.util.ArrayList<java.lang.Object>(this.rawList));
			}
			return new BuilderSingularOnRecord<T>(children, scarves, rawList);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderSingularOnRecord.BuilderSingularOnRecordBuilder(children=" + this.children + ", scarves=" + this.scarves + ", rawList=" + this.rawList + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static <T> BuilderSingularOnRecord.BuilderSingularOnRecordBuilder<T> builder() {
		return new BuilderSingularOnRecord.BuilderSingularOnRecordBuilder<T>();
	}
}
