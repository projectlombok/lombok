class LockedGeneratedStaticMismatch {
  private static final @java.lang.SuppressWarnings("all") @lombok.Generated java.util.concurrent.locks.Lock $LOCK = new java.util.concurrent.locks.ReentrantLock();
  <clinit>() {
  }
  LockedGeneratedStaticMismatch() {
    super();
  }
  static @lombok.Locked void test() {
    LockedGeneratedStaticMismatch.$LOCK.lock();
    try
      {
        System.out.println("one");
      }
    finally
      {
        LockedGeneratedStaticMismatch.$LOCK.unlock();
      }
  }
  @lombok.Locked("$LOCK") void test2() {
    System.out.println("two");
  }
}
class LockedUserStaticMismatch {
  private static final java.util.concurrent.locks.Lock userLock = new java.util.concurrent.locks.ReentrantLock();
  <clinit>() {
  }
  LockedUserStaticMismatch() {
    super();
  }
  static @lombok.Locked("userLock") void test() {
    LockedUserStaticMismatch.userLock.lock();
    try
      {
        System.out.println("one");
      }
    finally
      {
        LockedUserStaticMismatch.userLock.unlock();
      }
  }
  @lombok.Locked("userLock") void test2() {
    LockedUserStaticMismatch.userLock.lock();
    try
      {
        System.out.println("two");
      }
    finally
      {
        LockedUserStaticMismatch.userLock.unlock();
      }
  }
}
