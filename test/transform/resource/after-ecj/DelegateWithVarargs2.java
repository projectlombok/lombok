import lombok.experimental.Delegate;
class DelegateWithVarargs2 {
  public class B {
    public B() {
      super();
    }
    public void varargs(Object[]... keys) {
    }
  }
  private @Delegate DelegateWithVarargs2.B bar;
  DelegateWithVarargs2() {
    super();
  }
  public @java.lang.SuppressWarnings("all") void varargs(final java.lang.Object[]... keys) {
    this.bar.varargs(keys);
  }
}
