// version 14:
import lombok.Setter;
public @Setter record SetterOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
}
