public class BuilderDefaultsWarnings {
	long x = System.currentTimeMillis();
	final int y = 5;
	int z;
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	BuilderDefaultsWarnings(final long x, final int z) {
		this.x = x;
		this.z = z;
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public static class BuilderDefaultsWarningsBuilder {
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		private long x;
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		private int z;
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		BuilderDefaultsWarningsBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		public BuilderDefaultsWarningsBuilder x(final long x) {
			this.x = x;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		public BuilderDefaultsWarningsBuilder z(final int z) {
			this.z = z;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		public BuilderDefaultsWarnings build() {
			return new BuilderDefaultsWarnings(x, z);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		public java.lang.String toString() {
			return "BuilderDefaultsWarnings.BuilderDefaultsWarningsBuilder(x=" + this.x + ", z=" + this.z + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public static BuilderDefaultsWarningsBuilder builder() {
		return new BuilderDefaultsWarningsBuilder();
	}
}
