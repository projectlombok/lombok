class SetterWithDollar1 {
  @lombok.Setter int $i;
  public @java.lang.SuppressWarnings("all") void set$i(final int $i) {
    this.$i = $i;
  }
  SetterWithDollar1() {
    super();
  }
}
class SetterWithDollar2 {
  @lombok.Setter int $i;
  @lombok.Setter int i;
  public @java.lang.SuppressWarnings("all") void set$i(final int $i) {
    this.$i = $i;
  }
  public @java.lang.SuppressWarnings("all") void setI(final int i) {
    this.i = i;
  }
  SetterWithDollar2() {
    super();
  }
}
