import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
public class WeirdJavadoc {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class WeirdJavadocBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated String test;
    @java.lang.SuppressWarnings("all") @lombok.Generated WeirdJavadocBuilder() {
      super();
    }
    /**
     * @param test Copy this
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated WeirdJavadoc.WeirdJavadocBuilder test(final String test) {
      this.test = test;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated WeirdJavadoc build() {
      return new WeirdJavadoc(this.test);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (("WeirdJavadoc.WeirdJavadocBuilder(test=" + this.test) + ")");
    }
  }
  private @Getter @Setter String test;
  @Builder WeirdJavadoc(String test) {
    super();
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated WeirdJavadoc.WeirdJavadocBuilder builder() {
    return new WeirdJavadoc.WeirdJavadocBuilder();
  }
  /**
   * This is the real comment
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated String getTest() {
    return this.test;
  }
  /**
   * This is the real comment
   * @param test Copy this
   */
  public @java.lang.SuppressWarnings("all") @lombok.Generated void setTest(final String test) {
    this.test = test;
  }
}
