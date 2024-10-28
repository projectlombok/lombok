class Setter {
  static @lombok.Setter boolean foo;
  static @lombok.Setter int bar;
  <clinit>() {
  }
  Setter() {
    super();
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated void setFoo(final boolean foo) {
    Setter.foo = foo;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated void setBar(final int bar) {
    Setter.bar = bar;
  }
}
