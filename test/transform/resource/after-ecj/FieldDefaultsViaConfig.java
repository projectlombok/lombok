class FieldDefaultsViaConfig1 {
  private final int x;
  private @lombok.experimental.NonFinal int y;
  FieldDefaultsViaConfig1(int x) {
    super();
    this.x = x;
  }
}
@lombok.experimental.FieldDefaults(level = lombok.AccessLevel.PROTECTED) class FieldDefaultsViaConfig2 {
  final @lombok.experimental.PackagePrivate int x = 2;
  protected final int y = 0;
  FieldDefaultsViaConfig2() {
    super();
  }
}
