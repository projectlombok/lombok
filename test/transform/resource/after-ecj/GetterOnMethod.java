import lombok.Getter;
class GetterOnMethod {
  @lombok.Getter() int i;
  @lombok.Getter() int j;
  public @Deprecated @java.lang.SuppressWarnings("all") int getI() {
    return this.i;
  }
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") int getJ() {
    return this.j;
  }
  GetterOnMethod() {
    super();
  }
}
@lombok.Getter() class GetterOnClassOnMethod {
  int i;
  int j;
  public @java.lang.SuppressWarnings("all") int getI() {
    return this.i;
  }
  public @java.lang.SuppressWarnings("all") int getJ() {
    return this.j;
  }
  GetterOnClassOnMethod() {
    super();
  }
}
@lombok.Getter() class GetterOnClassAndOnAField {
  int i;
  @lombok.Getter() int j;
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") int getJ() {
    return this.j;
  }
  public @java.lang.SuppressWarnings("all") int getI() {
    return this.i;
  }
  GetterOnClassAndOnAField() {
    super();
  }
}
