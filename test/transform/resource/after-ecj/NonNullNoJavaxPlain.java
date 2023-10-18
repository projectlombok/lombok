import java.lang.annotation.*;
@lombok.RequiredArgsConstructor @lombok.Getter @lombok.Setter class NonNullNoJavaxPlain {
  @javax.annotation.Nonnull int i;
  @javax.annotation.Nonnull String s;
  public @java.lang.SuppressWarnings("all") NonNullNoJavaxPlain() {
    super();
  }
  public @javax.annotation.Nonnull @java.lang.SuppressWarnings("all") int getI() {
    return this.i;
  }
  public @javax.annotation.Nonnull @java.lang.SuppressWarnings("all") String getS() {
    return this.s;
  }
  public @java.lang.SuppressWarnings("all") void setI(final @javax.annotation.Nonnull int i) {
    this.i = i;
  }
  public @java.lang.SuppressWarnings("all") void setS(final @javax.annotation.Nonnull String s) {
    this.s = s;
  }
}
