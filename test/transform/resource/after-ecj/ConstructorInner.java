class ConstructorInner {
  static @lombok.AllArgsConstructor(staticName = "of") class Inner {
    private @java.lang.SuppressWarnings("all") Inner() {
      super();
    }
    public static @java.lang.SuppressWarnings("all") ConstructorInner.Inner of() {
      return new ConstructorInner.Inner();
    }
  }
  ConstructorInner() {
    super();
  }
}