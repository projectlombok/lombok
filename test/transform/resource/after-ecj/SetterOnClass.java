@lombok.Setter class SetterOnClass1 {
  @lombok.Setter(lombok.AccessLevel.NONE) boolean isNone;
  boolean isPublic;
  public @java.lang.SuppressWarnings("all") void setPublic(final boolean isPublic) {
    this.isPublic = isPublic;
  }
  SetterOnClass1() {
    super();
  }
}
@lombok.Setter(lombok.AccessLevel.PROTECTED) class SetterOnClass2 {
  @lombok.Setter(lombok.AccessLevel.NONE) boolean isNone;
  boolean isProtected;
  @lombok.Setter(lombok.AccessLevel.PACKAGE) boolean isPackage;
  @java.lang.SuppressWarnings("all") void setPackage(final boolean isPackage) {
    this.isPackage = isPackage;
  }
  protected @java.lang.SuppressWarnings("all") void setProtected(final boolean isProtected) {
    this.isProtected = isProtected;
  }
  SetterOnClass2() {
    super();
  }
}
@lombok.Setter(lombok.AccessLevel.PACKAGE) class SetterOnClass3 {
  @lombok.Setter(lombok.AccessLevel.NONE) boolean isNone;
  boolean isPackage;
  @java.lang.SuppressWarnings("all") void setPackage(final boolean isPackage) {
    this.isPackage = isPackage;
  }
  SetterOnClass3() {
    super();
  }
}
@lombok.Setter(lombok.AccessLevel.PRIVATE) class SetterOnClass4 {
  @lombok.Setter(lombok.AccessLevel.NONE) boolean isNone;
  boolean isPrivate;
  private @java.lang.SuppressWarnings("all") void setPrivate(final boolean isPrivate) {
    this.isPrivate = isPrivate;
  }
  SetterOnClass4() {
    super();
  }
}
@lombok.Setter(lombok.AccessLevel.PUBLIC) class SetterOnClass5 {
  @lombok.Setter(lombok.AccessLevel.NONE) boolean isNone;
  boolean isPublic;
  public @java.lang.SuppressWarnings("all") void setPublic(final boolean isPublic) {
    this.isPublic = isPublic;
  }
  SetterOnClass5() {
    super();
  }
}
@lombok.Setter class SetterOnClass6 {
  String couldBeNull;
  @lombok.NonNull String nonNull;
  public @java.lang.SuppressWarnings("all") void setCouldBeNull(final String couldBeNull) {
    this.couldBeNull = couldBeNull;
  }
  public @java.lang.SuppressWarnings("all") void setNonNull(final @lombok.NonNull String nonNull) {
    if ((nonNull == null))
        throw new java.lang.NullPointerException("nonNull");
    this.nonNull = nonNull;
  }
  SetterOnClass6() {
    super();
  }
}
