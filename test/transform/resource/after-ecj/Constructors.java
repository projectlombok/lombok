@lombok.RequiredArgsConstructor class RequiredArgsConstructor1 {
  final int x;
  String name;
  public @java.beans.ConstructorProperties({"x"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") RequiredArgsConstructor1(final int x) {
    super();
    this.x = x;
  }
}
@lombok.RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED) class RequiredArgsConstructorAccess {
  final int x;
  String name;
  protected @java.beans.ConstructorProperties({"x"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") RequiredArgsConstructorAccess(final int x) {
    super();
    this.x = x;
  }
}
@lombok.RequiredArgsConstructor(staticName = "staticname") class RequiredArgsConstructorStaticName {
  final int x;
  String name;
  private @java.beans.ConstructorProperties({"x"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") RequiredArgsConstructorStaticName(final int x) {
    super();
    this.x = x;
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") RequiredArgsConstructorStaticName staticname(final int x) {
    return new RequiredArgsConstructorStaticName(x);
  }
}
@lombok.RequiredArgsConstructor() class RequiredArgsConstructorWithAnnotations {
  final int x;
  String name;
  public @Deprecated @java.beans.ConstructorProperties({"x"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") RequiredArgsConstructorWithAnnotations(final int x) {
    super();
    this.x = x;
  }
}
@lombok.AllArgsConstructor class AllArgsConstructor1 {
  final int x;
  String name;
  public @java.beans.ConstructorProperties({"x", "name"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") AllArgsConstructor1(final int x, final String name) {
    super();
    this.x = x;
    this.name = name;
  }
}
@lombok.NoArgsConstructor class NoArgsConstructor1 {
  int x;
  String name;
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") NoArgsConstructor1() {
    super();
  }
}
@lombok.RequiredArgsConstructor(staticName = "of") class RequiredArgsConstructorStaticNameGenerics<T extends Number> {
  final T x;
  String name;
  private @java.beans.ConstructorProperties({"x"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") RequiredArgsConstructorStaticNameGenerics(final T x) {
    super();
    this.x = x;
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") <T extends Number>RequiredArgsConstructorStaticNameGenerics<T> of(final T x) {
    return new RequiredArgsConstructorStaticNameGenerics<T>(x);
  }
}
@lombok.RequiredArgsConstructor(staticName = "of") class RequiredArgsConstructorStaticNameGenerics2<T extends Number> {
  final Class<T> x;
  String name;
  private @java.beans.ConstructorProperties({"x"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") RequiredArgsConstructorStaticNameGenerics2(final Class<T> x) {
    super();
    this.x = x;
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") <T extends Number>RequiredArgsConstructorStaticNameGenerics2<T> of(final Class<T> x) {
    return new RequiredArgsConstructorStaticNameGenerics2<T>(x);
  }
}
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PACKAGE) class AllArgsConstructorPackageAccess {
  final String x;
  @java.beans.ConstructorProperties({"x"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") AllArgsConstructorPackageAccess(final String x) {
    super();
    this.x = x;
  }
}
@lombok.NoArgsConstructor(force = true) class NoArgsConstructor2 {
  final int x;
  final double y;
  final char c;
  final boolean b;
  final float f;
  final String s;
  byte z;
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") NoArgsConstructor2() {
    super();
    this.x = 0;
    this.y = 0D;
    this.c = '\0';
    this.b = false;
    this.f = 0F;
    this.s = null;
  }
}