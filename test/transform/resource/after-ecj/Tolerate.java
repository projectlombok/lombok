import java.util.regex.Pattern;
@lombok.Setter @lombok.Getter class Tolerate {
  private Pattern pattern;
  Tolerate() {
    super();
  }
  public @lombok.experimental.Tolerate void setPattern(String pattern) {
    setPattern(Pattern.compile(pattern));
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setPattern(final Pattern pattern) {
    this.pattern = pattern;
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated Pattern getPattern() {
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
  public @java.lang.SuppressWarnings("all") @lombok.Generated Pattern getPattern() {
    return this.pattern;
  }
  /**
   * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated Tolerate2 withPattern(final Pattern pattern) {
    return ((this.pattern == pattern) ? this : new Tolerate2(pattern));
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated Tolerate2(final Pattern pattern) {
    super();
    this.pattern = pattern;
  }
}
