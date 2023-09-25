import java.util.concurrent.locks.*;
class LockedName {
  private final Lock basicLock = new ReentrantLock();
  private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
  LockedName() {
    super();
  }
  @lombok.Locked("basicLock") void test() {
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
  @lombok.Locked.Read("rwLock") void test2() {
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
  @lombok.Locked.Write("rwLock") void test3() {
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
