class SimpleTypeResolutionFail {
  private @Getter int x;
  SimpleTypeResolutionFail() {
  }
}
class SimpleTypeResolutionSuccess {
  private @lombok.Getter int x;
  SimpleTypeResolutionSuccess() {
  }
  public @java.lang.SuppressWarnings("all") int getX() {
    return this.x;
  }
}
