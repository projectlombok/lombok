import lombok.AllArgsConstructor;
public @AllArgsConstructor(staticName = "of") class StaticConstructor {
  String name;
  private @java.lang.SuppressWarnings("all") @lombok.Generated StaticConstructor(final String name) {
    super();
    this.name = name;
  }
  public static @org.checkerframework.checker.nullness.qual.NonNull @java.lang.SuppressWarnings("all") @lombok.Generated StaticConstructor of(final String name) {
    return new StaticConstructor(name);
  }
}
