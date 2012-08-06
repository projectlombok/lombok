class Getter {
  static @lombok.Getter boolean foo;
  static @lombok.Getter int bar;
  <clinit>() {
  }
  Getter() {
    super();
  }
  public static @java.lang.SuppressWarnings("all") boolean isFoo() {
    return Getter.foo;
  }
  public static @java.lang.SuppressWarnings("all") int getBar() {
    return Getter.bar;
  }
}
