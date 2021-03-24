import lombok.Synchronized;
public record SynchronizedInRecord(java $lock, String a) {
  private final java.lang.Object $lock = new java.lang.Object[0];
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  public SynchronizedInRecord(String a, String b) {
    super();
    .a = a;
    .b = b;
  }
  public @Synchronized void foo() {
    String foo = "bar";
  }
}
