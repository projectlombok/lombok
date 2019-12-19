import java.util.List;
final class BuilderAndValueWithSetterPrefix {
	private final int zero = 0;
	@java.lang.SuppressWarnings("all")
	BuilderAndValueWithSetterPrefix() {
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderAndValueWithSetterPrefixBuilder {
		@java.lang.SuppressWarnings("all")
		BuilderAndValueWithSetterPrefixBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderAndValueWithSetterPrefix build() {
			return new BuilderAndValueWithSetterPrefix();
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderAndValueWithSetterPrefix.BuilderAndValueWithSetterPrefixBuilder()";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderAndValueWithSetterPrefix.BuilderAndValueWithSetterPrefixBuilder builder() {
		return new BuilderAndValueWithSetterPrefix.BuilderAndValueWithSetterPrefixBuilder();
	}
	@java.lang.SuppressWarnings("all")
	public int getZero() {
		return this.zero;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof BuilderAndValueWithSetterPrefix)) return false;
		final BuilderAndValueWithSetterPrefix other = (BuilderAndValueWithSetterPrefix) o;
		if (this.getZero() != other.getZero()) return false;
		return true;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getZero();
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "BuilderAndValueWithSetterPrefix(zero=" + this.getZero() + ")";
	}
}

class BuilderAndDataWithSetterPrefix {
	private final int zero = 0;
	@java.lang.SuppressWarnings("all")
	BuilderAndDataWithSetterPrefix() {
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderAndDataWithSetterPrefixBuilder {
		@java.lang.SuppressWarnings("all")
		BuilderAndDataWithSetterPrefixBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderAndDataWithSetterPrefix build() {
			return new BuilderAndDataWithSetterPrefix();
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderAndDataWithSetterPrefix.BuilderAndDataWithSetterPrefixBuilder()";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderAndDataWithSetterPrefix.BuilderAndDataWithSetterPrefixBuilder builder() {
		return new BuilderAndDataWithSetterPrefix.BuilderAndDataWithSetterPrefixBuilder();
	}
	@java.lang.SuppressWarnings("all")
	public int getZero() {
		return this.zero;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof BuilderAndDataWithSetterPrefix)) return false;
		final BuilderAndDataWithSetterPrefix other = (BuilderAndDataWithSetterPrefix) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (this.getZero() != other.getZero()) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof BuilderAndDataWithSetterPrefix;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getZero();
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public java.lang.String toString() {
		return "BuilderAndDataWithSetterPrefix(zero=" + this.getZero() + ")";
	}
}
