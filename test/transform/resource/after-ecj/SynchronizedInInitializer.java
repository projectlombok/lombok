import lombok.Synchronized;
public class SynchronizedInInitializer {
  public static final Runnable SYNCHRONIZED = new Runnable() {
    private final @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.Object $lock = new java.lang.Object[0];
    public @Override @Synchronized void run() {
      synchronized (this.$lock)
        {
          System.out.println("test");
        }
    }
  };
  <clinit>() {
  }
  public SynchronizedInInitializer() {
  }
}
