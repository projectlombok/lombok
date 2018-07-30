//CONF: lombok.noArgsConstructor.extraPrivate = true
import lombok.NoArgsConstructor;
final class ConstructorsWithBuilderDefaults<T> {
	private final java.util.List<T> z;
	private final T x;
	private final T q;
	@java.lang.SuppressWarnings("all")
	private static <T> java.util.List<T> $default$z() {
		return new java.util.ArrayList<T>();
	}
	@java.lang.SuppressWarnings("all")
	private static <T> T $default$x() {
		return null;
	}
	@java.lang.SuppressWarnings("all")
	ConstructorsWithBuilderDefaults(final java.util.List<T> z, final T x, final T q) {
		this.z = z;
		this.x = x;
		this.q = q;
	}
	@java.lang.SuppressWarnings("all")
	public static class ConstructorsWithBuilderDefaultsBuilder<T> {
		@java.lang.SuppressWarnings("all")
		private boolean z$set;
		@java.lang.SuppressWarnings("all")
		private java.util.List<T> z;
		@java.lang.SuppressWarnings("all")
		private boolean x$set;
		@java.lang.SuppressWarnings("all")
		private T x;
		@java.lang.SuppressWarnings("all")
		private T q;
		@java.lang.SuppressWarnings("all")
		ConstructorsWithBuilderDefaultsBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public ConstructorsWithBuilderDefaultsBuilder<T> z(final java.util.List<T> z) {
			this.z = z;
			z$set = true;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public ConstructorsWithBuilderDefaultsBuilder<T> x(final T x) {
			this.x = x;
			x$set = true;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public ConstructorsWithBuilderDefaultsBuilder<T> q(final T q) {
			this.q = q;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public ConstructorsWithBuilderDefaults<T> build() {
			java.util.List<T> z = this.z;
			if (!z$set) z = ConstructorsWithBuilderDefaults.<T>$default$z();
			T x = this.x;
			if (!x$set) x = ConstructorsWithBuilderDefaults.<T>$default$x();
			return new ConstructorsWithBuilderDefaults<T>(z, x, q);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "ConstructorsWithBuilderDefaults.ConstructorsWithBuilderDefaultsBuilder(z=" + this.z + ", x=" + this.x + ", q=" + this.q + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static <T> ConstructorsWithBuilderDefaultsBuilder<T> builder() {
		return new ConstructorsWithBuilderDefaultsBuilder<T>();
	}
	@java.lang.SuppressWarnings("all")
	private ConstructorsWithBuilderDefaults() {
		this.q = null;
		this.z = ConstructorsWithBuilderDefaults.$default$z();
		this.x = ConstructorsWithBuilderDefaults.$default$x();
	}
	@java.lang.SuppressWarnings("all")
	public java.util.List<T> getZ() {
		return this.z;
	}
	@java.lang.SuppressWarnings("all")
	public T getX() {
		return this.x;
	}
	@java.lang.SuppressWarnings("all")
	public T getQ() {
		return this.q;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof ConstructorsWithBuilderDefaults)) return false;
		final ConstructorsWithBuilderDefaults<?> other = (ConstructorsWithBuilderDefaults<?>) o;
		final java.lang.Object this$z = this.getZ();
		final java.lang.Object other$z = other.getZ();
		if (this$z == null ? other$z != null : !this$z.equals(other$z)) return false;
		final java.lang.Object this$x = this.getX();
		final java.lang.Object other$x = other.getX();
		if (this$x == null ? other$x != null : !this$x.equals(other$x)) return false;
		final java.lang.Object this$q = this.getQ();
		final java.lang.Object other$q = other.getQ();
		if (this$q == null ? other$q != null : !this$q.equals(other$q)) return false;
		return true;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final java.lang.Object $z = this.getZ();
		result = result * PRIME + ($z == null ? 43 : $z.hashCode());
		final java.lang.Object $x = this.getX();
		result = result * PRIME + ($x == null ? 43 : $x.hashCode());
		final java.lang.Object $q = this.getQ();
		result = result * PRIME + ($q == null ? 43 : $q.hashCode());
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "ConstructorsWithBuilderDefaults(z=" + this.getZ() + ", x=" + this.getX() + ", q=" + this.getQ() + ")";
	}
}
