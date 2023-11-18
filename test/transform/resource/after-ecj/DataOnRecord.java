// version 14:
import lombok.Data;
public @Data record DataOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
}
