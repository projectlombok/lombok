class LockedGeneratedTypeMismatch {
  private final @java.lang.SuppressWarnings("all") @lombok.Generated java.util.concurrent.locks.Lock $lock = new java.util.concurrent.locks.ReentrantLock();
  LockedGeneratedTypeMismatch() {
    super();
  }
  @lombok.Locked void test() {
    this.$lock.lock();
    try
      {
        System.out.println("one");
      }
    finally
      {
        this.$lock.unlock();
      }
  }
  @lombok.Locked.Read void test2() {
    System.out.println("two");
  }
}
class LockedUserTypeMismatch {
  private final java.util.concurrent.locks.Lock userLock = new java.util.concurrent.locks.ReentrantLock();
  LockedUserTypeMismatch() {
    super();
  }
  @lombok.Locked("userLock") void test() {
    this.userLock.lock();
    try
      {
        System.out.println("one");
      }
    finally
      {
        this.userLock.unlock();
      }
  }
  @lombok.Locked.Read("userLock") void test2() {
    this.userLock.readLock().lock();
    try
      {
        System.out.println("two");
      }
    finally
      {
        this.userLock.readLock().unlock();
      }
  }
}
