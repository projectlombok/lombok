class LockedName {
  private final java.util.concurrent.locks.ReentrantLock basicLock = new java.util.concurrent.locks.ReentrantLock();
  private final java.util.concurrent.locks.ReentrantReadWriteLock rwLock = new java.util.concurrent.locks.ReentrantReadWriteLock();
  LockedName() {
    super();
  }
  @lombok.experimental.Locked("basicLock") void test() {
    this.basicLock.lock();
    try
      {
        System.out.println("one");
      }
    finally
      {
        this.basicLock.unlock();
      }
  }
  @lombok.experimental.Locked.Read("rwLock") void test2() {
    this.rwLock.readLock().lock();
    try
      {
        System.out.println("two");
      }
    finally
      {
        this.rwLock.readLock().unlock();
      }
  }
  @lombok.experimental.Locked.Write("rwLock") void test3() {
    this.rwLock.writeLock().lock();
    try
      {
        System.out.println("three");
      }
    finally
      {
        this.rwLock.writeLock().unlock();
      }
  }
}
