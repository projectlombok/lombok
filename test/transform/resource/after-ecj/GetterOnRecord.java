// version 14:
import lombok.Getter;
public @Getter record GetterOnRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;

  public @java.lang.SuppressWarnings("all") String getA() {
    return this.a;
  }
  public @java.lang.SuppressWarnings("all") String getB() {
    return this.b;
  }
}
