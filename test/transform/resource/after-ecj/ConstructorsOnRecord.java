// version 14:
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
public @AllArgsConstructor @RequiredArgsConstructor @NoArgsConstructor record ConstructorsOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
}
