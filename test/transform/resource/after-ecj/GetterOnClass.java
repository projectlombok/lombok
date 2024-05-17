@lombok.Getter class GetterOnClass1 {
  @lombok.Getter(lombok.AccessLevel.NONE) boolean isNone;
  boolean isPublic;
  GetterOnClass1() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated boolean isPublic() {
    return this.isPublic;
  }
}
@lombok.Getter(lombok.AccessLevel.PROTECTED) class GetterOnClass2 {
  @lombok.Getter(lombok.AccessLevel.NONE) boolean isNone;
  boolean isProtected;
  @lombok.Getter(lombok.AccessLevel.PACKAGE) boolean isPackage;
  GetterOnClass2() {
    super();
  }
  @java.lang.SuppressWarnings("all") @lombok.Generated boolean isPackage() {
    return this.isPackage;
  }
  protected @java.lang.SuppressWarnings("all") @lombok.Generated boolean isProtected() {
    return this.isProtected;
  }
}
@lombok.Getter(lombok.AccessLevel.PACKAGE) class GetterOnClass3 {
  @lombok.Getter(lombok.AccessLevel.NONE) boolean isNone;
  boolean isPackage;
  GetterOnClass3() {
    super();
  }
  @java.lang.SuppressWarnings("all") @lombok.Generated boolean isPackage() {
    return this.isPackage;
  }
}
@lombok.Getter(lombok.AccessLevel.PRIVATE) class GetterOnClass4 {
  @lombok.Getter(lombok.AccessLevel.NONE) boolean isNone;
  boolean isPrivate;
  GetterOnClass4() {
    super();
  }
  private @java.lang.SuppressWarnings("all") @lombok.Generated boolean isPrivate() {
    return this.isPrivate;
  }
}
@lombok.Getter(lombok.AccessLevel.PUBLIC) class GetterOnClass5 {
  @lombok.Getter(lombok.AccessLevel.NONE) boolean isNone;
  boolean isPublic;
  GetterOnClass5() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated boolean isPublic() {
    return this.isPublic;
  }
}
@lombok.Getter class GetterOnClass6 {
  String couldBeNull;
  @lombok.NonNull String nonNull;
  GetterOnClass6() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated String getCouldBeNull() {
    return this.couldBeNull;
  }
  public @lombok.NonNull @java.lang.SuppressWarnings("all") @lombok.Generated String getNonNull() {
    return this.nonNull;
  }
}
