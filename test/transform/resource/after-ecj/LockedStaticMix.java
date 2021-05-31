class LockedGeneratedStaticMismatch {
  private static final java.util.concurrent.locks.ReentrantLock LOCK = new java.util.concurrent.locks.ReentrantLock();
  <clinit>() {
  }
  LockedGeneratedStaticMismatch() {
    super();
  }
  static @lombok.experimental.Locked void test() {
    LockedGeneratedStaticMismatch.LOCK.lock();
    try
      {
        System.out.println("one");
      }
    finally
      {
        LockedGeneratedStaticMismatch.LOCK.unlock();
      }
  }
  @lombok.experimental.Locked("LOCK") void test2() {
    System.out.println("two");
  }
}
class LockedUserStaticMismatch {
  private static final java.util.concurrent.locks.ReentrantLock userLock = new java.util.concurrent.locks.ReentrantLock();
  <clinit>() {
  }
  LockedUserStaticMismatch() {
    super();
  }
  static @lombok.experimental.Locked("userLock") void test() {
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
  @lombok.experimental.Locked("userLock") void test2() {
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
