import lombok.Synchronized;
public record SynchronizedInRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  private final java.lang.Object $lock = new java.lang.Object[0];
  public SynchronizedInRecord(String a, String b) {
    super();
    .a = a;
    .b = b;
  }
  public @Synchronized void foo() {
    String foo = "bar";
  }
}
