import lombok.Locked;
public class LockedInInitializer {
  public static final Runnable LOCKED = new Runnable() {
    private final @java.lang.SuppressWarnings("all") @lombok.Generated java.util.concurrent.locks.Lock $lock = new java.util.concurrent.locks.ReentrantLock();
    public @Override @Locked void run() {
      this.$lock.lock();
      try
        {
          System.out.println("test");
        }
      finally
        {
          this.$lock.unlock();
        }
    }
  };
  public static final Runnable LOCKED_READ = new Runnable() {
    private final @java.lang.SuppressWarnings("all") @lombok.Generated java.util.concurrent.locks.ReadWriteLock $lock = new java.util.concurrent.locks.ReentrantReadWriteLock();
    public @Override @Locked.Read void run() {
      this.$lock.readLock().lock();
      try
        {
          System.out.println("test");
        }
      finally
        {
          this.$lock.readLock().unlock();
        }
    }
  };
  public static final Runnable LOCKED_WRITE = new Runnable() {
    private final @java.lang.SuppressWarnings("all") @lombok.Generated java.util.concurrent.locks.ReadWriteLock $lock = new java.util.concurrent.locks.ReentrantReadWriteLock();
    public @Override @Locked.Write void run() {
      this.$lock.writeLock().lock();
      try
        {
          System.out.println("test");
        }
      finally
        {
          this.$lock.writeLock().unlock();
        }
    }
  };
  <clinit>() {
  }
  public LockedInInitializer() {
  }
}
