//version 8:
import org.jspecify.annotations.NonNull;

class DataWithJspecifyTypeUseNonNull {
	@NonNull
	private String prop1;
	private java.lang.@NonNull String prop2;

	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public DataWithJspecifyTypeUseNonNull(@NonNull final String prop1, final java.lang.@NonNull String prop2) {
		if (prop1 == null) {
			throw new java.lang.NullPointerException("prop1 is marked non-null but is null");
		}
		if (prop2 == null) {
			throw new java.lang.NullPointerException("prop2 is marked non-null but is null");
		}
		this.prop1 = prop1;
		this.prop2 = prop2;
	}

	@NonNull
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public String getProp1() {
		return this.prop1;
	}

	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public java.lang.@NonNull String getProp2() {
		return this.prop2;
	}

	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setProp1(@NonNull final String prop1) {
		if (prop1 == null) {
			throw new java.lang.NullPointerException("prop1 is marked non-null but is null");
		}
		this.prop1 = prop1;
	}

	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public void setProp2(final java.lang.@NonNull String prop2) {
		if (prop2 == null) {
			throw new java.lang.NullPointerException("prop2 is marked non-null but is null");
		}
		this.prop2 = prop2;
	}

	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof DataWithJspecifyTypeUseNonNull)) return false;
		final DataWithJspecifyTypeUseNonNull other = (DataWithJspecifyTypeUseNonNull) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		final java.lang.Object this$prop1 = this.getProp1();
		final java.lang.Object other$prop1 = other.getProp1();
		if (this$prop1 == null ? other$prop1 != null : !this$prop1.equals(other$prop1)) return false;
		final java.lang.Object this$prop2 = this.getProp2();
		final java.lang.Object other$prop2 = other.getProp2();
		if (this$prop2 == null ? other$prop2 != null : !this$prop2.equals(other$prop2)) return false;
		return true;
	}

	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof DataWithJspecifyTypeUseNonNull;
	}

	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final java.lang.Object $prop1 = this.getProp1();
		result = result * PRIME + ($prop1 == null ? 43 : $prop1.hashCode());
		final java.lang.Object $prop2 = this.getProp2();
		result = result * PRIME + ($prop2 == null ? 43 : $prop2.hashCode());
		return result;
	}

	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public java.lang.String toString() {
		return "DataWithJspecifyTypeUseNonNull(prop1=" + this.getProp1() + ", prop2=" + this.getProp2() + ")";
	}
}
