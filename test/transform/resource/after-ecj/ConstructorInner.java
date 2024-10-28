class ConstructorInner {
  static @lombok.AllArgsConstructor(staticName = "of") class Inner {
    private @java.lang.SuppressWarnings("all") @lombok.Generated Inner() {
      super();
    }
    public static @java.lang.SuppressWarnings("all") @lombok.Generated ConstructorInner.Inner of() {
      return new ConstructorInner.Inner();
    }
  }
  ConstructorInner() {
    super();
  }
}
