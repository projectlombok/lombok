import lombok.experimental.Delegate;
class DelegatePlain {
  private static class FooImpl implements Foo {
    private FooImpl() {
      super();
    }
    public void foo() {
    }
    public void bar(java.util.ArrayList<java.lang.String> list) {
    }
  }
  private static class BarImpl implements Bar {
    private BarImpl() {
      super();
    }
    public void bar(java.util.ArrayList<java.lang.String> list) {
    }
  }
  private static interface Foo extends Bar {
    void foo();
  }
  private static interface Bar {
    void bar(java.util.ArrayList<java.lang.String> list);
  }
  private final @Delegate(types = Bar.class) BarImpl bar = new BarImpl();
  private final @Delegate(types = Foo.class,excludes = Bar.class) FooImpl foo = new FooImpl();
  DelegatePlain() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void bar(final java.util.ArrayList<java.lang.String> list) {
    this.bar.bar(list);
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void foo() {
    this.foo.foo();
  }
}
