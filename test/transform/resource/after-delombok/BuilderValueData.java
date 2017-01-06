import java.util.List;
final class BuilderAndValue {
	private final int zero = 0;
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	BuilderAndValue() {
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	public static class BuilderAndValueBuilder {
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		@lombok.Generated
		BuilderAndValueBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		@lombok.Generated
		public BuilderAndValue build() {
			return new BuilderAndValue();
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderAndValue.BuilderAndValueBuilder()";
		}
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	public static BuilderAndValueBuilder builder() {
		return new BuilderAndValueBuilder();
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	public int getZero() {
		return this.zero;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof BuilderAndValue)) return false;
		final BuilderAndValue other = (BuilderAndValue) o;
		if (this.getZero() != other.getZero()) return false;
		return true;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getZero();
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	public java.lang.String toString() {
		return "BuilderAndValue(zero=" + this.getZero() + ")";
	}
}

class BuilderAndData {
	private final int zero = 0;
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	BuilderAndData() {
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	public static class BuilderAndDataBuilder {
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		@lombok.Generated
		BuilderAndDataBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		@lombok.Generated
		public BuilderAndData build() {
			return new BuilderAndData();
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderAndData.BuilderAndDataBuilder()";
		}
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	public static BuilderAndDataBuilder builder() {
		return new BuilderAndDataBuilder();
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	public int getZero() {
		return this.zero;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof BuilderAndData)) return false;
		final BuilderAndData other = (BuilderAndData) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (this.getZero() != other.getZero()) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof BuilderAndData;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getZero();
		return result;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	public java.lang.String toString() {
		return "BuilderAndData(zero=" + this.getZero() + ")";
	}
}
