import lombok.Delegate;
abstract class DelegateOnMethods {
  public static interface Bar {
    void bar(java.util.ArrayList<java.lang.String> list);
  }
  public @java.lang.SuppressWarnings("all") void bar(final java.util.ArrayList<java.lang.String> list) {
    this.getBar().bar(list);
  }
  DelegateOnMethods() {
    super();
  }
  public abstract @Delegate Bar getBar();
}