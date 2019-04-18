public final class BuilderDefaults {
	private final int x;
	private final String name;
	private final long z;
	@java.lang.SuppressWarnings("all")
	private static int $default$x() {
		return 10;
	}
	@java.lang.SuppressWarnings("all")
	private static long $default$z() {
		return System.currentTimeMillis();
	}
	@java.lang.SuppressWarnings("all")
	BuilderDefaults(final int x, final String name, final long z) {
		this.x = x;
		this.name = name;
		this.z = z;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderDefaultsBuilder {
		@java.lang.SuppressWarnings("all")
		private boolean x$set;
		@java.lang.SuppressWarnings("all")
		private int x;
		@java.lang.SuppressWarnings("all")
		private String name;
		@java.lang.SuppressWarnings("all")
		private boolean z$set;
		@java.lang.SuppressWarnings("all")
		private long z;
		@java.lang.SuppressWarnings("all")
		BuilderDefaultsBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderDefaultsBuilder x(final int x) {
			this.x = x;
			x$set = true;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderDefaultsBuilder name(final String name) {
			this.name = name;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderDefaultsBuilder z(final long z) {
			this.z = z;
			z$set = true;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderDefaults build() {
			int x = this.x;
			if (!x$set) x = BuilderDefaults.$default$x();
			long z = this.z;
			if (!z$set) z = BuilderDefaults.$default$z();
			return new BuilderDefaults(x, name, z);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderDefaults.BuilderDefaultsBuilder(x=" + this.x + ", name=" + this.name + ", z=" + this.z + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderDefaultsBuilder builder() {
		return new BuilderDefaultsBuilder();
	}
	@java.lang.SuppressWarnings("all")
	public int getX() {
		return this.x;
	}
	@java.lang.SuppressWarnings("all")
	public String getName() {
		return this.name;
	}
	@java.lang.SuppressWarnings("all")
	public long getZ() {
		return this.z;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof BuilderDefaults)) return false;
		final BuilderDefaults other = (BuilderDefaults) o;
		if (this.getX() != other.getX()) return false;
		final java.lang.Object this$name = this.getName();
		final java.lang.Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		if (this.getZ() != other.getZ()) return false;
		return true;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getX();
		final java.lang.Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final long $z = this.getZ();
		result = result * PRIME + (int) ($z >>> 32 ^ $z);
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "BuilderDefaults(x=" + this.getX() + ", name=" + this.getName() + ", z=" + this.getZ() + ")";
	}
}
