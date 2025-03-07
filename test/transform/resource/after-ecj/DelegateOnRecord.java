// version 14:
import lombok.experimental.Delegate;
record DelegateOnRecord(Runnable runnable) {
/* Implicit */  private final Runnable runnable;
  public @java.lang.SuppressWarnings("all") @lombok.Generated void run() {
    this.runnable.run();
  }
}
