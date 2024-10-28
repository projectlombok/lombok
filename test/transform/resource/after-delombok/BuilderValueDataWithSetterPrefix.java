import java.util.List;
final class BuilderAndValueWithSetterPrefix {
	private final int zero = 0;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderAndValueWithSetterPrefix() {
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderAndValueWithSetterPrefixBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderAndValueWithSetterPrefixBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderAndValueWithSetterPrefix build() {
			return new BuilderAndValueWithSetterPrefix();
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderAndValueWithSetterPrefix.BuilderAndValueWithSetterPrefixBuilder()";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderAndValueWithSetterPrefix.BuilderAndValueWithSetterPrefixBuilder builder() {
		return new BuilderAndValueWithSetterPrefix.BuilderAndValueWithSetterPrefixBuilder();
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int getZero() {
		return this.zero;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof BuilderAndValueWithSetterPrefix)) return false;
		final BuilderAndValueWithSetterPrefix other = (BuilderAndValueWithSetterPrefix) o;
		if (this.getZero() != other.getZero()) return false;
		return true;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getZero();
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public java.lang.String toString() {
		return "BuilderAndValueWithSetterPrefix(zero=" + this.getZero() + ")";
	}
}
class BuilderAndDataWithSetterPrefix {
	private final int zero = 0;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderAndDataWithSetterPrefix() {
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderAndDataWithSetterPrefixBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderAndDataWithSetterPrefixBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderAndDataWithSetterPrefix build() {
			return new BuilderAndDataWithSetterPrefix();
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderAndDataWithSetterPrefix.BuilderAndDataWithSetterPrefixBuilder()";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderAndDataWithSetterPrefix.BuilderAndDataWithSetterPrefixBuilder builder() {
		return new BuilderAndDataWithSetterPrefix.BuilderAndDataWithSetterPrefixBuilder();
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int getZero() {
		return this.zero;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof BuilderAndDataWithSetterPrefix)) return false;
		final BuilderAndDataWithSetterPrefix other = (BuilderAndDataWithSetterPrefix) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (this.getZero() != other.getZero()) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof BuilderAndDataWithSetterPrefix;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getZero();
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public java.lang.String toString() {
		return "BuilderAndDataWithSetterPrefix(zero=" + this.getZero() + ")";
	}
}
