// version 14:
import lombok.experimental.UtilityClass;
public @UtilityClass record UtilityClassOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  public UtilityClassOnRecord(String a, String b) {
    super();
    .a = a;
    .b = b;
  }
}
