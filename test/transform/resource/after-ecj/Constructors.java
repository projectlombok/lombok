@lombok.RequiredArgsConstructor class RequiredArgsConstructor1 {
  final int x;
  String name;
  public @java.beans.ConstructorProperties({"x"}) @java.lang.SuppressWarnings("all") RequiredArgsConstructor1(final int x) {
    super();
    this.x = x;
  }
}
@lombok.RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED) class RequiredArgsConstructorAccess {
  final int x;
  String name;
  protected @java.beans.ConstructorProperties({"x"}) @java.lang.SuppressWarnings("all") RequiredArgsConstructorAccess(final int x) {
    super();
    this.x = x;
  }
}
@lombok.RequiredArgsConstructor(staticName = "staticname") class RequiredArgsConstructorStaticName {
  final int x;
  String name;
  private @java.lang.SuppressWarnings("all") RequiredArgsConstructorStaticName(final int x) {
    super();
    this.x = x;
  }
  public static @java.lang.SuppressWarnings("all") RequiredArgsConstructorStaticName staticname(final int x) {
    return new RequiredArgsConstructorStaticName(x);
  }
}
@lombok.AllArgsConstructor class AllArgsConstructor1 {
  final int x;
  String name;
  public @java.beans.ConstructorProperties({"x", "name"}) @java.lang.SuppressWarnings("all") AllArgsConstructor1(final int x, final String name) {
    super();
    this.x = x;
    this.name = name;
  }
}
@lombok.NoArgsConstructor class NoArgsConstructor1 {
  int x;
  String name;
  public @java.lang.SuppressWarnings("all") NoArgsConstructor1() {
    super();
  }
}
@lombok.RequiredArgsConstructor(staticName = "of") class RequiredArgsConstructorStaticNameGenerics<T extends Number> {
  final T x;
  String name;
  private @java.lang.SuppressWarnings("all") RequiredArgsConstructorStaticNameGenerics(final T x) {
    super();
    this.x = x;
  }
  public static @java.lang.SuppressWarnings("all") <T extends Number>RequiredArgsConstructorStaticNameGenerics<T> of(final T x) {
    return new RequiredArgsConstructorStaticNameGenerics<T>(x);
  }
}