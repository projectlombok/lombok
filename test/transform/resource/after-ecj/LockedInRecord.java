import lombok.Locked;
public record LockedInRecord(String a, String b) {
/* Implicit */  private final String a;
/* Implicit */  private final String b;
  private final @java.lang.SuppressWarnings("all") @lombok.Generated java.util.concurrent.locks.Lock $lock = new java.util.concurrent.locks.ReentrantLock();
  public @Locked void foo() {
    String foo = "bar";
  }
}
