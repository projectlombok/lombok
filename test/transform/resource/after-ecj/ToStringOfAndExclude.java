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
