import java.lang.annotation.*;
@lombok.RequiredArgsConstructor @lombok.Getter @lombok.Setter class NonNullPlain {
  public @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE}) @Retention(RetentionPolicy.CLASS) @interface NotNull {
  }
  @lombok.NonNull int i;
  @lombok.NonNull String s;
  @NotNull Object o;
  public @java.beans.ConstructorProperties({"i", "s"}) @java.lang.SuppressWarnings("all") NonNullPlain(final @lombok.NonNull int i, final @lombok.NonNull String s) {
    super();
    if ((s == null))
        throw new java.lang.NullPointerException("s");
    this.i = i;
    this.s = s;
  }
  public @lombok.NonNull @java.lang.SuppressWarnings("all") int getI() {
    return this.i;
  }
  public @lombok.NonNull @java.lang.SuppressWarnings("all") String getS() {
    return this.s;
  }
  public @java.lang.SuppressWarnings("all") Object getO() {
    return this.o;
  }
  public @java.lang.SuppressWarnings("all") void setI(final @lombok.NonNull int i) {
    this.i = i;
  }
  public @java.lang.SuppressWarnings("all") void setS(final @lombok.NonNull String s) {
    if ((s == null))
        throw new java.lang.NullPointerException("s");
    this.s = s;
  }
  public @java.lang.SuppressWarnings("all") void setO(final Object o) {
    this.o = o;
  }
}