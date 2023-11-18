// version 14:
import lombok.Value;
public @Value record ValueOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
}
