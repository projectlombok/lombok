class SynchronizedName {
  private Object read = new Object();
  private static Object READ = new Object();
  <clinit>() {
  }
  SynchronizedName() {
    super();
  }
  @lombok.Synchronized("read") void test1() {
    synchronized (this.read)
      {
        System.out.println("one");
      }
  }
  @lombok.Synchronized("write") void test2() {
    System.out.println("two");
  }
  static @lombok.Synchronized("read") void test3() {
    synchronized (SynchronizedName.read)
      {
        System.out.println("three");
      }
  }
  @lombok.Synchronized("READ") void test4() {
    synchronized (this.READ)
      {
        System.out.println("four");
      }
  }
  @lombok.Synchronized(value = "read") void test5() {
    synchronized (this.read)
      {
        System.out.println("five");
      }
  }
}