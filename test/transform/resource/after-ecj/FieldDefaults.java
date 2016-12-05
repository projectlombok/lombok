@lombok.experimental.FieldDefaults(makeFinal = true) class FieldDefaults1 {
  static int STATIC = 3;
  final int x;
  @lombok.experimental.NonFinal int y;
  <clinit>() {
  }
  FieldDefaults1(int x) {
    super();
    this.x = x;
  }
}
@lombok.experimental.FieldDefaults(level = lombok.AccessLevel.PRIVATE) class FieldDefaults2 {
  static int STATIC = 3;
  @lombok.experimental.PackagePrivate int x;
  private int y;
  <clinit>() {
  }
  FieldDefaults2() {
    super();
  }
}