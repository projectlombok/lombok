import lombok.experimental.Delegate;
abstract class DelegateOnMethods {
  public static interface Bar {
    void bar(java.util.ArrayList<java.lang.String> list);
  }
  DelegateOnMethods() {
    super();
  }
  public abstract @Delegate Bar getBar();
  public @java.lang.SuppressWarnings("all") void bar(final java.util.ArrayList<java.lang.String> list) {
    this.getBar().bar(list);
  }
}
