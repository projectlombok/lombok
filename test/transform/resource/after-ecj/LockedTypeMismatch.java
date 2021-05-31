class LockedGeneratedTypeMismatch {
  private final java.util.concurrent.locks.ReentrantLock lock = new java.util.concurrent.locks.ReentrantLock();
  LockedGeneratedTypeMismatch() {
    super();
  }
  @lombok.experimental.Locked void test() {
    this.lock.lock();
    try
      {
        System.out.println("one");
      }
    finally
      {
        this.lock.unlock();
      }
  }
  @lombok.experimental.Locked.Read void test2() {
    System.out.println("two");
  }
}
class LockedUserTypeMismatch {
  private final java.util.concurrent.locks.ReentrantLock userLock = new java.util.concurrent.locks.ReentrantLock();
  LockedUserTypeMismatch() {
    super();
  }
  @lombok.experimental.Locked("userLock") void test() {
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
  @lombok.experimental.Locked.Read("userLock") void test2() {
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
