class GetterWithDollar1 {
  @lombok.Getter int $i;
  GetterWithDollar1() {
    super();
  }
  public @java.lang.SuppressWarnings("all") int get$i() {
    return this.$i;
  }
}
class GetterWithDollar2 {
  @lombok.Getter int $i;
  @lombok.Getter int i;
  GetterWithDollar2() {
    super();
  }
  public @java.lang.SuppressWarnings("all") int get$i() {
    return this.$i;
  }
  public @java.lang.SuppressWarnings("all") int getI() {
    return this.i;
  }
}
