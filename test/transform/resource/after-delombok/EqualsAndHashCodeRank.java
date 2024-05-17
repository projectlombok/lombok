public class EqualsAndHashCodeRank {
	int a;
	int b;
	int c;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCodeRank)) return false;
		final EqualsAndHashCodeRank other = (EqualsAndHashCodeRank) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (this.a != other.a) return false;
		if (this.c != other.c) return false;
		if (this.b != other.b) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof EqualsAndHashCodeRank;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.a;
		result = result * PRIME + this.c;
		result = result * PRIME + this.b;
		return result;
	}
}
