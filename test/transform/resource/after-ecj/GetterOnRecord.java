// version 14:
import lombok.Getter;
public @Getter record GetterOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
}
