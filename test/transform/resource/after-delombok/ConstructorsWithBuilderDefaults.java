final class ConstructorsWithBuilderDefaults {
	private final int x;
	private final int y;
	@java.lang.SuppressWarnings("all")
	private static int $default$x() {
		return 5;
	}
	@java.lang.SuppressWarnings("all")
	public static class ConstructorsWithBuilderDefaultsBuilder {
		@java.lang.SuppressWarnings("all")
		private boolean x$set;
		@java.lang.SuppressWarnings("all")
		private int x$value;
		@java.lang.SuppressWarnings("all")
		private int y;
		@java.lang.SuppressWarnings("all")
		ConstructorsWithBuilderDefaultsBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public ConstructorsWithBuilderDefaultsBuilder x(final int x) {
			this.x$value = x;
			x$set = true;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public ConstructorsWithBuilderDefaultsBuilder y(final int y) {
			this.y = y;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public ConstructorsWithBuilderDefaults build() {
			int x$value = this.x$value;
			if (!x$set) x$value = ConstructorsWithBuilderDefaults.$default$x();
			return new ConstructorsWithBuilderDefaults(x$value, y);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "ConstructorsWithBuilderDefaults.ConstructorsWithBuilderDefaultsBuilder(x$value=" + this.x$value + ", y=" + this.y + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static ConstructorsWithBuilderDefaultsBuilder builder() {
		return new ConstructorsWithBuilderDefaultsBuilder();
	}
	@java.lang.SuppressWarnings("all")
	public int getX() {
		return this.x;
	}
	@java.lang.SuppressWarnings("all")
	public int getY() {
		return this.y;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof ConstructorsWithBuilderDefaults)) return false;
		final ConstructorsWithBuilderDefaults other = (ConstructorsWithBuilderDefaults) o;
		if (this.getX() != other.getX()) return false;
		if (this.getY() != other.getY()) return false;
		return true;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getX();
		result = result * PRIME + this.getY();
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "ConstructorsWithBuilderDefaults(x=" + this.getX() + ", y=" + this.getY() + ")";
	}
	@java.lang.SuppressWarnings("all")
	public ConstructorsWithBuilderDefaults() {
		this.y = 0;
		this.x = ConstructorsWithBuilderDefaults.$default$x();
	}
	@java.lang.SuppressWarnings("all")
	public ConstructorsWithBuilderDefaults(final int x, final int y) {
		this.x = x;
		this.y = y;
	}
}
