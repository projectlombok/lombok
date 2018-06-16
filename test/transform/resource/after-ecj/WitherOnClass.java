@lombok.experimental.Wither class WitherOnClass1 {
  @lombok.experimental.Wither(lombok.AccessLevel.NONE) boolean isNone;
  boolean isPublic;
  WitherOnClass1(boolean isNone, boolean isPublic) {
    super();
  }
  public @java.lang.SuppressWarnings("all") WitherOnClass1 withPublic(final boolean isPublic) {
    return ((this.isPublic == isPublic) ? this : new WitherOnClass1(this.isNone, isPublic));
  }
}
@lombok.experimental.Wither(lombok.AccessLevel.PROTECTED) class WitherOnClass2 {
  @lombok.experimental.Wither(lombok.AccessLevel.NONE) boolean isNone;
  boolean isProtected;
  @lombok.experimental.Wither(lombok.AccessLevel.PACKAGE) boolean isPackage;
  WitherOnClass2(boolean isNone, boolean isProtected, boolean isPackage) {
    super();
  }
  @java.lang.SuppressWarnings("all") WitherOnClass2 withPackage(final boolean isPackage) {
    return ((this.isPackage == isPackage) ? this : new WitherOnClass2(this.isNone, this.isProtected, isPackage));
  }
  protected @java.lang.SuppressWarnings("all") WitherOnClass2 withProtected(final boolean isProtected) {
    return ((this.isProtected == isProtected) ? this : new WitherOnClass2(this.isNone, isProtected, this.isPackage));
  }
}
@lombok.experimental.Wither class WitherOnClass3 {
  String couldBeNull;
  @lombok.NonNull String nonNull;
  WitherOnClass3(String couldBeNull, String nonNull) {
    super();
  }
  public @java.lang.SuppressWarnings("all") WitherOnClass3 withCouldBeNull(final String couldBeNull) {
    return ((this.couldBeNull == couldBeNull) ? this : new WitherOnClass3(couldBeNull, this.nonNull));
  }
  public @java.lang.SuppressWarnings("all") WitherOnClass3 withNonNull(final @lombok.NonNull String nonNull) {
    if ((nonNull == null))
        {
          throw new java.lang.NullPointerException("nonNull is marked @NonNull but is null");
        }
    return ((this.nonNull == nonNull) ? this : new WitherOnClass3(this.couldBeNull, nonNull));
  }
}
@lombok.experimental.Wither @lombok.experimental.Accessors(prefix = "f") class WitherOnClass4 {
  final int fX = 10;
  final int fY;
  WitherOnClass4(int y) {
    super();
    this.fY = y;
  }
  public @java.lang.SuppressWarnings("all") WitherOnClass4 withY(final int fY) {
    return ((this.fY == fY) ? this : new WitherOnClass4(fY));
  }
}
