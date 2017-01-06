import lombok.AccessLevel;
import lombok.Getter;
@Getter class GetterNone {
  int i;
  @Getter(AccessLevel.NONE) int foo;
  GetterNone() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated int getI() {
    return this.i;
  }
}
