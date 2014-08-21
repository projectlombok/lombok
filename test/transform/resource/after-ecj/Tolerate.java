import java.util.regex.Pattern;
@lombok.Setter @lombok.Getter class Tolerate {
  private Pattern pattern;
  Tolerate() {
    super();
  }
  public @lombok.experimental.Tolerate void setPattern(String pattern) {
    setPattern(Pattern.compile(pattern));
  }
  public @java.lang.SuppressWarnings("all") void setPattern(final Pattern pattern) {
    this.pattern = pattern;
  }
  public @java.lang.SuppressWarnings("all") Pattern getPattern() {
    return this.pattern;
  }
}
@lombok.Getter @lombok.experimental.Wither @lombok.AllArgsConstructor class Tolerate2 {
  private final Pattern pattern;
  public @lombok.experimental.Tolerate Tolerate2 withPattern(String pattern) {
    return withPattern(Pattern.compile(pattern));
  }
  public Tolerate2 withPattern(String nameGlob, String extensionGlob) {
    return withPattern(((nameGlob.replace("*", ".*") + "\\.") + extensionGlob.replace("*", ".*")));
  }
  public @java.lang.SuppressWarnings("all") Pattern getPattern() {
    return this.pattern;
  }
  public @java.lang.SuppressWarnings("all") Tolerate2 withPattern(final Pattern pattern) {
    return ((this.pattern == pattern) ? this : new Tolerate2(pattern));
  }
  public @java.beans.ConstructorProperties({"pattern"}) @java.lang.SuppressWarnings("all") Tolerate2(final Pattern pattern) {
    super();
    this.pattern = pattern;
  }
}
final @lombok.Getter @lombok.AllArgsConstructor @lombok.EqualsAndHashCode class Tolerate3 {
  private final java.math.RoundingMode mode;
  public @lombok.experimental.Tolerate int hashCode() {
    return (123456789 * mode.ordinal());
  }
  public @java.lang.SuppressWarnings("all") java.math.RoundingMode getMode() {
    return this.mode;
  }
  public @java.beans.ConstructorProperties({"mode"}) @java.lang.SuppressWarnings("all") Tolerate3(final java.math.RoundingMode mode) {
    super();
    this.mode = mode;
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") boolean equals(final java.lang.Object o) {
    if ((o == this))
        return true;
    if ((! (o instanceof Tolerate3)))
        return false;
    final @java.lang.SuppressWarnings("all") Tolerate3 other = (Tolerate3) o;
    final java.lang.Object this$mode = this.getMode();
    final java.lang.Object other$mode = other.getMode();
    if (((this$mode == null) ? (other$mode != null) : (! this$mode.equals(other$mode))))
        return false;
    return true;
  }
}
