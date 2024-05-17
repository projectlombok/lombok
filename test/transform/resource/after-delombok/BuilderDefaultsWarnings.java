//skip-idempotent
public class BuilderDefaultsWarnings {
	long x = System.currentTimeMillis();
	final int y = 5;
	int z;
	java.util.List<String> items;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderDefaultsWarnings(final long x, final int z, final java.util.List<String> items) {
		this.x = x;
		this.z = z;
		this.items = items;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderDefaultsWarningsBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private long x;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int z;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private java.util.ArrayList<String> items;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderDefaultsWarningsBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderDefaultsWarnings.BuilderDefaultsWarningsBuilder x(final long x) {
			this.x = x;
			return this;
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderDefaultsWarnings.BuilderDefaultsWarningsBuilder z(final int z) {
			this.z = z;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderDefaultsWarnings.BuilderDefaultsWarningsBuilder item(final String item) {
			if (this.items == null) this.items = new java.util.ArrayList<String>();
			this.items.add(item);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderDefaultsWarnings.BuilderDefaultsWarningsBuilder items(final java.util.Collection<? extends String> items) {
			if (items == null) {
				throw new java.lang.NullPointerException("items cannot be null");
			}
			if (this.items == null) this.items = new java.util.ArrayList<String>();
			this.items.addAll(items);
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderDefaultsWarnings.BuilderDefaultsWarningsBuilder clearItems() {
			if (this.items != null) this.items.clear();
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderDefaultsWarnings build() {
			java.util.List<String> items;
			switch (this.items == null ? 0 : this.items.size()) {
			case 0: 
				items = java.util.Collections.emptyList();
				break;
			case 1: 
				items = java.util.Collections.singletonList(this.items.get(0));
				break;
			default: 
				items = java.util.Collections.unmodifiableList(new java.util.ArrayList<String>(this.items));
			}
			return new BuilderDefaultsWarnings(this.x, this.z, items);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderDefaultsWarnings.BuilderDefaultsWarningsBuilder(x=" + this.x + ", z=" + this.z + ", items=" + this.items + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderDefaultsWarnings.BuilderDefaultsWarningsBuilder builder() {
		return new BuilderDefaultsWarnings.BuilderDefaultsWarningsBuilder();
	}
}
class NoBuilderButHasDefaults {
	private final long z = 5;
	public NoBuilderButHasDefaults() {
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class NoBuilderButHasDefaultsBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		NoBuilderButHasDefaultsBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public NoBuilderButHasDefaults build() {
			return new NoBuilderButHasDefaults();
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "NoBuilderButHasDefaults.NoBuilderButHasDefaultsBuilder()";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static NoBuilderButHasDefaults.NoBuilderButHasDefaultsBuilder builder() {
		return new NoBuilderButHasDefaults.NoBuilderButHasDefaultsBuilder();
	}
}
