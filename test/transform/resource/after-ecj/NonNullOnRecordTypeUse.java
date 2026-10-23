// version 14:
import lombok.NonNull;
public record NonNullOnRecordTypeUse(int a, int b, int c) {
  public @java.lang.SuppressWarnings("all") @lombok.Generated NonNullOnRecordTypeUse(@NonNull int[] a, int @NonNull [] b, int[] @NonNull [] c) {
    super();
    if ((b == null))
        {
          throw new java.lang.NullPointerException("b is marked non-null but is null");
        }
    this.a = a;
    this.b = b;
    this.c = c;
  }
}
