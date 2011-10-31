import lombok.Synchronized;
class SynchronizedPlain1 {
  private final java.lang.Object $lock = new java.lang.Object[0];
  SynchronizedPlain1() {
    super();
  }
  @lombok.Synchronized void test() {
    synchronized (this.$lock)
      {
        System.out.println("one");
      }
  }
  @Synchronized void test2() {
    synchronized (this.$lock)
      {
        System.out.println("two");
      }
  }
}
class SynchronizedPlain2 {
  private static final java.lang.Object $LOCK = new java.lang.Object[0];
  <clinit>() {
  }
  SynchronizedPlain2() {
    super();
  }
  static @lombok.Synchronized void test() {
    synchronized (SynchronizedPlain2.$LOCK)
      {
        System.out.println("three");
      }
  }
  static @Synchronized void test2() {
    synchronized (SynchronizedPlain2.$LOCK)
      {
        System.out.println("four");
      }
  }
}
