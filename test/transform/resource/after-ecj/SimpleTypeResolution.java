class SimpleTypeResolutionFail {
  private @Getter int x;
  SimpleTypeResolutionFail() {
  }
}
class SimpleTypeResolutionSuccess {
  private @lombok.Getter int x;
  SimpleTypeResolutionSuccess() {
  }
  public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated int getX() {
    return this.x;
  }
}
