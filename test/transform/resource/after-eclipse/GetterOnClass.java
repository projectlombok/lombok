@lombok.Getter class GetterOnClass1 {
  @lombok.Getter(lombok.AccessLevel.NONE) boolean isNone;
  boolean isPublic;
  public @java.lang.SuppressWarnings("all") boolean isPublic() {
    return this.isPublic;
  }
  GetterOnClass1() {
    super();
  }

}
@lombok.Getter(lombok.AccessLevel.PROTECTED) class GetterOnClass2 {
  @lombok.Getter(lombok.AccessLevel.NONE) boolean isNone;
  boolean isProtected;
  @lombok.Getter(lombok.AccessLevel.PACKAGE) boolean isPackage;
  @java.lang.SuppressWarnings("all") boolean isPackage() {
    return this.isPackage;
  }
  protected @java.lang.SuppressWarnings("all") boolean isProtected() {
    return this.isProtected;
  }
  GetterOnClass2() {
    super();
  }
}
@lombok.Getter(lombok.AccessLevel.PACKAGE) class GetterOnClass3 {
  @lombok.Getter(lombok.AccessLevel.NONE) boolean isNone;
  boolean isPackage;
  @java.lang.SuppressWarnings("all") boolean isPackage() {
    return this.isPackage;
  }
  GetterOnClass3() {
    super();
  }
}
@lombok.Getter(lombok.AccessLevel.PRIVATE) class GetterOnClass4 {
  @lombok.Getter(lombok.AccessLevel.NONE) boolean isNone;
  boolean isPrivate;
  private @java.lang.SuppressWarnings("all") boolean isPrivate() {
    return this.isPrivate;
  }
  GetterOnClass4() {
    super();
  }
}
@lombok.Getter(lombok.AccessLevel.PUBLIC) class GetterOnClass5 {
  @lombok.Getter(lombok.AccessLevel.NONE) boolean isNone;
  boolean isPublic;
  public @java.lang.SuppressWarnings("all") boolean isPublic() {
    return this.isPublic;
  }
  GetterOnClass5() {
    super();
  }
}
@lombok.Getter class GetterOnClass6 {
  String couldBeNull;
  @lombok.NonNull String nonNull;
  public @java.lang.SuppressWarnings("all") String getCouldBeNull() {
    return this.couldBeNull;
  }
  public @lombok.NonNull @java.lang.SuppressWarnings("all") String getNonNull() {
    return this.nonNull;
  }
  GetterOnClass6() {
    super();
  }
}