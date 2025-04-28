import lombok.Builder;
import lombok.ToString;
import lombok.ToString.Exclude;
public @Builder @ToString class BuilderWithToStringExcludeAndToString {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderWithToStringExcludeAndToStringBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated String a;
    private @java.lang.SuppressWarnings("all") @lombok.Generated String secret;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExcludeAndToStringBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExcludeAndToString.BuilderWithToStringExcludeAndToStringBuilder a(final String a) {
      this.a = a;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExcludeAndToString.BuilderWithToStringExcludeAndToStringBuilder secret(final String secret) {
      this.secret = secret;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExcludeAndToString build() {
      return new BuilderWithToStringExcludeAndToString(this.a, this.secret);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (("BuilderWithToStringExcludeAndToString.BuilderWithToStringExcludeAndToStringBuilder(a=" + this.a) + ")");
    }
  }
  private String a;
  private @ToString.Exclude String secret;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExcludeAndToString(final String a, final String secret) {
    super();
    this.a = a;
    this.secret = secret;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExcludeAndToString.BuilderWithToStringExcludeAndToStringBuilder builder() {
    return new BuilderWithToStringExcludeAndToString.BuilderWithToStringExcludeAndToStringBuilder();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (("BuilderWithToStringExcludeAndToString(a=" + this.a) + ")");
  }
}
