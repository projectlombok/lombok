import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
public @lombok.ToString class ToStringArrayTypeAnnotations {
  @Target({ElementType.TYPE_USE}) @interface TA {
  }
  @TA int[] primitiveArray1;
  int @TA [] primitiveArray2;
  @TA Object[] objectArray1;
  Object @TA [] objectArray2;
  public ToStringArrayTypeAnnotations() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (((((((("ToStringArrayTypeAnnotations(primitiveArray1=" + java.util.Arrays.toString(this.primitiveArray1)) + ", primitiveArray2=") + java.util.Arrays.toString(this.primitiveArray2)) + ", objectArray1=") + java.util.Arrays.deepToString(this.objectArray1)) + ", objectArray2=") + java.util.Arrays.deepToString(this.objectArray2)) + ")");
  }
}
