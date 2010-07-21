import lombok.Getter;
class GetterPlain {
  @lombok.Getter int i;
  @Getter int foo;
  GetterPlain() {
    super();
  }
  public @java.lang.SuppressWarnings("all") int getI() {
    return this.i;
  }
  public @java.lang.SuppressWarnings("all") int getFoo() {
    return this.foo;
  }
}