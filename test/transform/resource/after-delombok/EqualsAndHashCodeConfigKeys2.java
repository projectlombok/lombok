class EqualsAndHashCodeConfigKeys2Object extends Object {
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCodeConfigKeys2Object)) return false;
		final EqualsAndHashCodeConfigKeys2Object other = (EqualsAndHashCodeConfigKeys2Object) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof EqualsAndHashCodeConfigKeys2Object;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public int hashCode() {
		int result = 1;
		return result;
	}
}
class EqualsAndHashCodeConfigKeys2Parent {
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCodeConfigKeys2Parent)) return false;
		final EqualsAndHashCodeConfigKeys2Parent other = (EqualsAndHashCodeConfigKeys2Parent) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof EqualsAndHashCodeConfigKeys2Parent;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public int hashCode() {
		int result = 1;
		return result;
	}
}
class EqualsAndHashCodeConfigKeys2 extends EqualsAndHashCodeConfigKeys2Parent {
	int x;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCodeConfigKeys2)) return false;
		final EqualsAndHashCodeConfigKeys2 other = (EqualsAndHashCodeConfigKeys2) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (!super.equals(o)) return false;
		if (this.x != other.x) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof EqualsAndHashCodeConfigKeys2;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + super.hashCode();
		result = result * PRIME + this.x;
		return result;
	}
}
