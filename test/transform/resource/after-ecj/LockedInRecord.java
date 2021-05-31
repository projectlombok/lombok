import lombok.experimental.Locked;
public record LockedInRecord(java lock, String a) {
  private final java.util.concurrent.locks.ReentrantLock lock = new java.util.concurrent.locks.ReentrantLock();
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  public LockedInRecord(String a, String b) {
    super();
    .a = a;
    .b = b;
  }
  public @Locked void foo() {
    String foo = "bar";
  }
}
