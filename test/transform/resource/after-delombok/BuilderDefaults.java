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
		private int x$value;
		@java.lang.SuppressWarnings("all")
		private String name;
		@java.lang.SuppressWarnings("all")
		private boolean z$set;
		@java.lang.SuppressWarnings("all")
		private long z$value;
		@java.lang.SuppressWarnings("all")
		BuilderDefaultsBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderDefaults.BuilderDefaultsBuilder x(final int x) {
			this.x$value = x;
			x$set = true;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderDefaults.BuilderDefaultsBuilder name(final String name) {
			this.name = name;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderDefaults.BuilderDefaultsBuilder z(final long z) {
			this.z$value = z;
			z$set = true;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderDefaults build() {
			int x$value = this.x$value;
			if (!this.x$set) x$value = BuilderDefaults.$default$x();
			long z$value = this.z$value;
			if (!this.z$set) z$value = BuilderDefaults.$default$z();
			return new BuilderDefaults(x$value, this.name, z$value);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderDefaults.BuilderDefaultsBuilder(x$value=" + this.x$value + ", name=" + this.name + ", z$value=" + this.z$value + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderDefaults.BuilderDefaultsBuilder builder() {
		return new BuilderDefaults.BuilderDefaultsBuilder();
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
