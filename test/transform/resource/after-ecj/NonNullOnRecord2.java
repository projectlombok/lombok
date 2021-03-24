// version 14:
import lombok.NonNull;
record NonNullOnRecord2(String a) {
/* Implicit */  private final String a;
  public NonNullOnRecord2(@NonNull String a) {
    super();
    if ((a == null))
        {
          throw new java.lang.NullPointerException("a is marked non-null but is null");
        }
    System.out.println("Hello");
    this.a = a;
  }
}
