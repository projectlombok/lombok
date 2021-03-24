// version 14:
import lombok.EqualsAndHashCode;
public @EqualsAndHashCode record EqualsAndHashCodeOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  public EqualsAndHashCodeOnRecord(String a, String b) {
    super();
    .a = a;
    .b = b;
  }
}
