@lombok.With class WithOnClass1 {
  @lombok.With(lombok.AccessLevel.NONE) boolean isNone;
  boolean isPublic;
  WithOnClass1(boolean isNone, boolean isPublic) {
    super();
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") WithOnClass1 withPublic(final boolean isPublic) {
    return ((this.isPublic == isPublic) ? this : new WithOnClass1(this.isNone, isPublic));
  }
}
@lombok.With(lombok.AccessLevel.PROTECTED) class WithOnClass2 {
  @lombok.With(lombok.AccessLevel.NONE) boolean isNone;
  boolean isProtected;
  @lombok.With(lombok.AccessLevel.PACKAGE) boolean isPackage;
  WithOnClass2(boolean isNone, boolean isProtected, boolean isPackage) {
    super();
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  @java.lang.SuppressWarnings("all") WithOnClass2 withPackage(final boolean isPackage) {
    return ((this.isPackage == isPackage) ? this : new WithOnClass2(this.isNone, this.isProtected, isPackage));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  protected @java.lang.SuppressWarnings("all") WithOnClass2 withProtected(final boolean isProtected) {
    return ((this.isProtected == isProtected) ? this : new WithOnClass2(this.isNone, isProtected, this.isPackage));
  }
}
@lombok.With class WithOnClass3 {
  String couldBeNull;
  @lombok.NonNull String nonNull;
  WithOnClass3(String couldBeNull, String nonNull) {
    super();
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") WithOnClass3 withCouldBeNull(final String couldBeNull) {
    return ((this.couldBeNull == couldBeNull) ? this : new WithOnClass3(couldBeNull, this.nonNull));
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") WithOnClass3 withNonNull(final @lombok.NonNull String nonNull) {
    if ((nonNull == null))
        {
          throw new java.lang.NullPointerException("nonNull is marked non-null but is null");
        }
    return ((this.nonNull == nonNull) ? this : new WithOnClass3(this.couldBeNull, nonNull));
  }
}
@lombok.With @lombok.experimental.Accessors(prefix = "f") class WithOnClass4 {
  final int fX = 10;
  final int fY;
  WithOnClass4(int y) {
    super();
    this.fY = y;
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") WithOnClass4 withY(final int fY) {
    return ((this.fY == fY) ? this : new WithOnClass4(fY));
  }
}
