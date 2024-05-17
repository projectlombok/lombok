import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
public @lombok.ToString class ToStringArray {
  int[] primitiveArray;
  Object[] objectArray;
  public ToStringArray() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (((("ToStringArray(primitiveArray=" + java.util.Arrays.toString(this.primitiveArray)) + ", objectArray=") + java.util.Arrays.deepToString(this.objectArray)) + ")");
  }
}
