import lombok.AccessLevel;
import lombok.Getter;
@Getter class GetterNone {
  int i;
  @Getter(AccessLevel.NONE) int foo;
  GetterNone() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated int getI() {
    return this.i;
  }
}
