class GetterWithDollar1 {
  @lombok.Getter int $i;
  public @java.lang.SuppressWarnings("all") int get$i() {
    return this.$i;
  }
  GetterWithDollar1() {
    super();
  }
}
class GetterWithDollar2 {
  @lombok.Getter int $i;
  @lombok.Getter int i;
  public @java.lang.SuppressWarnings("all") int get$i() {
    return this.$i;
  }
  public @java.lang.SuppressWarnings("all") int getI() {
    return this.i;
  }
  GetterWithDollar2() {
    super();
  }
}
