import lombok.Delegate;
class DelegateRecursionOuterMost {
  private final @Delegate DelegateRecursionCenter center = new DelegateRecursionCenter();
  DelegateRecursionOuterMost() {
    super();
  }
  public @java.lang.SuppressWarnings("all") void innerMostMethod() {
    this.center.innerMostMethod();
  }
}
class DelegateRecursionCenter {
  private final @Delegate DelegateRecursionInnerMost inner = new DelegateRecursionInnerMost();
  DelegateRecursionCenter() {
    super();
  }
  public @java.lang.SuppressWarnings("all") void innerMostMethod() {
    this.inner.innerMostMethod();
  }
}
class DelegateRecursionInnerMost {
  DelegateRecursionInnerMost() {
    super();
  }
  public void innerMostMethod() {
  }
}
