class LockedOnStatic<Z> {
  static class Inner {
    private static final java.util.concurrent.locks.Lock LCK = new java.util.concurrent.locks.ReentrantLock();
    <clinit>() {
    }
    Inner() {
      super();
    }
    public @lombok.Locked("LCK") void foo() {
      LockedOnStatic.Inner.LCK.lock();
      try
        {
          System.out.println();
        }
      finally
        {
          LockedOnStatic.Inner.LCK.unlock();
        }
    }
  }
  class Inner2 {
    private final java.util.concurrent.locks.ReentrantLock LCK = new java.util.concurrent.locks.ReentrantLock();
    Inner2() {
      super();
    }
    public @lombok.Locked("LCK") void foo() {
      this.LCK.lock();
      try
        {
          System.out.println();
        }
      finally
        {
          this.LCK.unlock();
        }
    }
  }
  LockedOnStatic() {
    super();
  }
}
