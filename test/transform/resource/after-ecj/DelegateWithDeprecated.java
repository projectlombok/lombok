import lombok.experimental.Delegate;
class DelegateWithDeprecated {
  private interface Bar {
    @Deprecated void deprecatedAnnotation();
    void deprecatedComment();
    void notDeprecated();
  }
  private @Delegate Bar bar;
  DelegateWithDeprecated() {
    super();
  }
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") void deprecatedAnnotation() {
    this.bar.deprecatedAnnotation();
  }
  public @java.lang.Deprecated @java.lang.SuppressWarnings("all") void deprecatedComment() {
    this.bar.deprecatedComment();
  }
  public @java.lang.SuppressWarnings("all") void notDeprecated() {
    this.bar.notDeprecated();
  }
}
