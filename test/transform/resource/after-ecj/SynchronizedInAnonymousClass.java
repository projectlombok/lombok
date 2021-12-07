import lombok.Synchronized;
public class SynchronizedInAnonymousClass {
  Object annonymous = new Object() {
    class Inner {
      private final java.lang.Object $lock = new java.lang.Object[0];
      Inner() {
        super();
      }
      public @Synchronized void foo() {
        synchronized (this.$lock)
          {
            String foo = "bar";
          }
      }
    }
    x() {
      super();
    }
  };
  public SynchronizedInAnonymousClass() {
    super();
  }
}
