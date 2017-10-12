@lombok.ToString(of = {"x"}) class ToStringOf {
  int x;
  int y;
  ToStringOf() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
    return (("ToStringOf(x=" + this.x) + ")");
  }
}
@lombok.ToString(exclude = {"y"}) class ToStringExclude {
  int x;
  int y;
  ToStringExclude() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
    return (("ToStringExclude(x=" + this.x) + ")");
  }
}
@lombok.ToString class ToStringOfField {
  @lombok.ToString.Of int x;
  int y;
  ToStringOfField() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
    return (("ToStringOfField(x=" + this.x) + ")");
  }
}
@lombok.ToString class ToStringExcludeField {
  int x;
  @lombok.ToString.Exclude int y;
  ToStringExcludeField() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
    return (("ToStringExcludeField(x=" + this.x) + ")");
  }
}
