//version 8:
import java.lang.annotation.*;
class EqualsAndHashCodeAnnotated {
	@Annotated
	int primitive;
	@Annotated
	Object object;
	int @Annotated [] primitiveValues;
	int @Annotated [] @Annotated [] morePrimitiveValues;
	Integer @Annotated [] objectValues;
	Integer @Annotated [] @Annotated [] moreObjectValues;
	@Target(ElementType.TYPE_USE)
	@Retention(RetentionPolicy.SOURCE)
	@interface Annotated {
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof EqualsAndHashCodeAnnotated)) return false;
		final EqualsAndHashCodeAnnotated other = (EqualsAndHashCodeAnnotated) o;
		if (!other.canEqual((java.lang.Object) this)) return false;
		if (this.primitive != other.primitive) return false;
		final java.lang.Object this$object = this.object;
		final java.lang.Object other$object = other.object;
		if (this$object == null ? other$object != null : !this$object.equals(other$object)) return false;
		if (!java.util.Arrays.equals(this.primitiveValues, other.primitiveValues)) return false;
		if (!java.util.Arrays.deepEquals(this.morePrimitiveValues, other.morePrimitiveValues)) return false;
		if (!java.util.Arrays.deepEquals(this.objectValues, other.objectValues)) return false;
		if (!java.util.Arrays.deepEquals(this.moreObjectValues, other.moreObjectValues)) return false;
		return true;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof EqualsAndHashCodeAnnotated;
	}
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.primitive;
		final java.lang.Object $object = this.object;
		result = result * PRIME + ($object == null ? 43 : $object.hashCode());
		result = result * PRIME + java.util.Arrays.hashCode(this.primitiveValues);
		result = result * PRIME + java.util.Arrays.deepHashCode(this.morePrimitiveValues);
		result = result * PRIME + java.util.Arrays.deepHashCode(this.objectValues);
		result = result * PRIME + java.util.Arrays.deepHashCode(this.moreObjectValues);
		return result;
	}
}
