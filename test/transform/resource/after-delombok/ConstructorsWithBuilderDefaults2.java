//CONF: lombok.noArgsConstructor.extraPrivate = true
import lombok.NoArgsConstructor;
final class ConstructorsWithBuilderDefaults {
	private final int x;
	@java.lang.SuppressWarnings("all")
	private static int $default$x() {
		return 5;
	}
	@java.lang.SuppressWarnings("all")
	ConstructorsWithBuilderDefaults(final int x) {
		this.x = x;
	}
	@java.lang.SuppressWarnings("all")
	public static class ConstructorsWithBuilderDefaultsBuilder {
		@java.lang.SuppressWarnings("all")
		private boolean x$set;
		@java.lang.SuppressWarnings("all")
		private int x;
		@java.lang.SuppressWarnings("all")
		ConstructorsWithBuilderDefaultsBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public ConstructorsWithBuilderDefaultsBuilder x(final int x) {
			this.x = x;
			x$set = true;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public ConstructorsWithBuilderDefaults build() {
			int x = this.x;
			if (!x$set) x = ConstructorsWithBuilderDefaults.$default$x();
			return new ConstructorsWithBuilderDefaults(x);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "ConstructorsWithBuilderDefaults.ConstructorsWithBuilderDefaultsBuilder(x=" + this.x + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static ConstructorsWithBuilderDefaultsBuilder builder() {
		return new ConstructorsWithBuilderDefaultsBuilder();
	}
	@java.lang.SuppressWarnings("all")
	private ConstructorsWithBuilderDefaults() {
		this.x = ConstructorsWithBuilderDefaults.$default$x();
	}
	@java.lang.SuppressWarnings("all")
	public int getX() {
		return this.x;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof ConstructorsWithBuilderDefaults)) return false;
		final ConstructorsWithBuilderDefaults other = (ConstructorsWithBuilderDefaults) o;
		if (this.getX() != other.getX()) return false;
		return true;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getX();
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "ConstructorsWithBuilderDefaults(x=" + this.getX() + ")";
	}
}