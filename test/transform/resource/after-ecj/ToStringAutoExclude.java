@lombok.ToString class ToStringAutoExclude {
  int x;
  String $a;
  transient String b;
  ToStringAutoExclude() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (((("ToStringAutoExclude(x=" + this.x) + ", b=") + this.b) + ")");
  }
}
@lombok.ToString class ToStringAutoExclude2 {
  int x;
  @lombok.ToString.Include String $a;
  transient String b;
  ToStringAutoExclude2() {
    super();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (((((("ToStringAutoExclude2(x=" + this.x) + ", $a=") + this.$a) + ", b=") + this.b) + ")");
  }
}
