class Setter1 {
  @lombok.Setter boolean foo;
  Setter1() {
    super();
  }
  void setFoo(boolean foo) {
  }
}
class Setter2 {
  @lombok.Setter boolean foo;
  Setter2() {
    super();
  }
  void setFoo(String foo) {
  }
}
class Setter3 {
  @lombok.Setter String foo;
  Setter3() {
    super();
  }
  void setFoo(boolean foo) {
  }
}
class Setter4 {
  @lombok.Setter String foo;
  Setter4() {
    super();
  }
  void setFoo(String foo) {
  }
}
class Setter5 {
  @lombok.Setter String foo;
  Setter5() {
    super();
  }
  void setFoo() {
  }
  public @java.lang.SuppressWarnings("all") void setFoo(final String foo) {
    this.foo = foo;
  }
}
class Setter6 {
  @lombok.Setter String foo;
  Setter6() {
    super();
  }
  void setFoo(String foo, int x) {
  }
  public @java.lang.SuppressWarnings("all") void setFoo(final String foo) {
    this.foo = foo;
  }
}
class Setter7 {
  @lombok.Setter String foo;
  Setter7() {
    super();
  }
  void setFoo(String foo, Object... x) {
  }
}
class Setter8 {
  @lombok.Setter boolean isFoo;
  Setter8() {
    super();
  }
  void setIsFoo(boolean foo) {
  }
}
class Setter9 {
  @lombok.Setter boolean isFoo;
  Setter9() {
    super();
  }
  void setFoo(boolean foo) {
  }
}
