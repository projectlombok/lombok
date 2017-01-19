import lombok.Getter;
class GetterPlain {
  @lombok.Getter int i;
  @Getter int foo;
  GetterPlain() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated int getI() {
    return this.i;
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated int getFoo() {
    return this.foo;
  }
}
