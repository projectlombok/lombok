class Getter {
  @lombok.Getter boolean foo;
  @lombok.Getter boolean isBar;
  @lombok.Getter boolean hasBaz;
  Getter() {
    super();
  }
  public @java.lang.SuppressWarnings("all") boolean isFoo() {
    return this.foo;
  }
  public @java.lang.SuppressWarnings("all") boolean isBar() {
    return this.isBar;
  }
  public @java.lang.SuppressWarnings("all") boolean hasBaz() {
    return this.hasBaz;
  }
}
class MoreGetter {
  @lombok.Getter boolean foo;
  MoreGetter() {
    super();
  }
  boolean hasFoo() {
    return true;
  }
}
