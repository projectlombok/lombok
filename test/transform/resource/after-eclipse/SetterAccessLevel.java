class SetterAccessLevel {
  @lombok.Setter(lombok.AccessLevel.NONE) boolean isNone;
  @lombok.Setter(lombok.AccessLevel.PRIVATE) boolean isPrivate;
  @lombok.Setter(lombok.AccessLevel.PACKAGE) boolean isPackage;
  @lombok.Setter(lombok.AccessLevel.PROTECTED) boolean isProtected;
  @lombok.Setter(lombok.AccessLevel.PUBLIC) boolean isPublic;
  @lombok.Setter(value = lombok.AccessLevel.PUBLIC) boolean value;
  private @java.lang.SuppressWarnings("all") void setPrivate(final boolean isPrivate) {
    this.isPrivate = isPrivate;
  }
  @java.lang.SuppressWarnings("all") void setPackage(final boolean isPackage) {
    this.isPackage = isPackage;
  }
  protected @java.lang.SuppressWarnings("all") void setProtected(final boolean isProtected) {
    this.isProtected = isProtected;
  }
  public @java.lang.SuppressWarnings("all") void setPublic(final boolean isPublic) {
    this.isPublic = isPublic;
  }
  public @java.lang.SuppressWarnings("all") void setValue(final boolean value) {
    this.value = value;
  }
  SetterAccessLevel() {
    super();
  }
}
