import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
public class ToStringArray {
	int[] primitiveArray;
	Object[] objectArray;
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public java.lang.String toString() {
		return "ToStringArray(primitiveArray=" + java.util.Arrays.toString(this.primitiveArray) + ", objectArray=" + java.util.Arrays.deepToString(this.objectArray) + ")";
	}
}
