class SynchronizedNameNoSuchField {
  private Object read = new Object();
  private static Object READ = new Object();
  <clinit>() {
  }
  SynchronizedNameNoSuchField() {
    super();
  }
  @lombok.Synchronized("write") void test2() {
    System.out.println("two");
  }
}
