import lombok.Delegate;
class DelegateRecursionOuterMost {
  private final @Delegate DelegateRecursionCenter center = new DelegateRecursionCenter();
  public @java.lang.SuppressWarnings("all") void innerMostMethod() {
    this.center.innerMostMethod();
  }
  DelegateRecursionOuterMost() {
    super();
  }
}
class DelegateRecursionCenter {
  private final @Delegate DelegateRecursionInnerMost inner = new DelegateRecursionInnerMost();
  public @java.lang.SuppressWarnings("all") void innerMostMethod() {
    this.inner.innerMostMethod();
  }
  DelegateRecursionCenter() {
    super();
  }
}
class DelegateRecursionInnerMost {
  DelegateRecursionInnerMost() {
    super();
  }
  public void innerMostMethod() {
  }
}