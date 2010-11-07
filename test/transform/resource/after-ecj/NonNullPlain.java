class NonNullPlain {
  @lombok.Setter @lombok.NonNull @lombok.Getter int i;
  @lombok.Getter @lombok.Setter @lombok.NonNull String s;
  public @java.lang.SuppressWarnings("all") void setI(final @lombok.NonNull int i) {
    this.i = i;
  }
  public @lombok.NonNull @java.lang.SuppressWarnings("all") int getI() {
    return this.i;
  }
  public @lombok.NonNull @java.lang.SuppressWarnings("all") String getS() {
    return this.s;
  }
  public @java.lang.SuppressWarnings("all") void setS(final @lombok.NonNull String s) {
    if ((s == null))
        throw new java.lang.NullPointerException("s");
    this.s = s;
  }
  NonNullPlain() {
    super();
  }
}