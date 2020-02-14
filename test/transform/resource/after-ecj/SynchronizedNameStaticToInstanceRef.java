class SynchronizedNameStaticToInstanceRef {
  private Object read = new Object();
  private static Object READ = new Object();
  <clinit>() {
  }
  SynchronizedNameStaticToInstanceRef() {
    super();
  }
  static @lombok.Synchronized("read") void test3() {
    System.out.println("three");
  }
}
