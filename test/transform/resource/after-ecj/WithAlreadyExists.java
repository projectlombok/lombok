class With1 {
  @lombok.With boolean foo;
  void withFoo(boolean foo) {
  }
  With1(boolean foo) {
    super();
  }
}
class With2 {
  @lombok.With boolean foo;
  void withFoo(String foo) {
  }
  With2(boolean foo) {
    super();
  }
}
class With3 {
  @lombok.With String foo;
  void withFoo(boolean foo) {
  }
  With3(String foo) {
    super();
  }
}
class With4 {
  @lombok.With String foo;
  void withFoo(String foo) {
  }
  With4(String foo) {
    super();
  }
}
class With5 {
  @lombok.With String foo;
  void withFoo() {
  }
  With5(String foo) {
    super();
  }
  public @java.lang.SuppressWarnings("all") With5 withFoo(final String foo) {
    return ((this.foo == foo) ? this : new With5(foo));
  }
}
class With6 {
  @lombok.With String foo;
  void withFoo(String foo, int x) {
  }
  With6(String foo) {
    super();
  }
  public @java.lang.SuppressWarnings("all") With6 withFoo(final String foo) {
    return ((this.foo == foo) ? this : new With6(foo));
  }
}
class With7 {
  @lombok.With String foo;
  void withFoo(String foo, Object... x) {
  }
  With7(String foo) {
    super();
  }
}
class With8 {
  @lombok.With boolean isFoo;
  void withIsFoo(boolean foo) {
  }
  With8(boolean foo) {
    super();
  }
}
class With9 {
  @lombok.With boolean isFoo;
  void withFoo(boolean foo) {
  }
  With9(boolean foo) {
    super();
  }
}
