@lombok.experimental.FieldDefaults(makeFinal = true) class FieldDefaults1 {
  final int x;
  @lombok.experimental.NonFinal int y;
  FieldDefaults1(int x) {
    super();
    this.x = x;
  }
}
@lombok.experimental.FieldDefaults(level = lombok.AccessLevel.PRIVATE) class FieldDefaults2 {
  @lombok.experimental.PackagePrivate int x;
  private int y;
  FieldDefaults2() {
    super();
  }
}