import lombok.Builder;
import lombok.ToString;
public @ToString(exclude = "secret") @Builder class BuilderWithToStringExcludeOld {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderWithToStringExcludeOldBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated String a;
    private @java.lang.SuppressWarnings("all") @lombok.Generated String secret;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExcludeOldBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExcludeOld.BuilderWithToStringExcludeOldBuilder a(final String a) {
      this.a = a;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExcludeOld.BuilderWithToStringExcludeOldBuilder secret(final String secret) {
      this.secret = secret;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExcludeOld build() {
      return new BuilderWithToStringExcludeOld(this.a, this.secret);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (("BuilderWithToStringExcludeOld.BuilderWithToStringExcludeOldBuilder(a=" + this.a) + ")");
    }
  }
  private String a;
  private String secret;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExcludeOld(final String a, final String secret) {
    super();
    this.a = a;
    this.secret = secret;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExcludeOld.BuilderWithToStringExcludeOldBuilder builder() {
    return new BuilderWithToStringExcludeOld.BuilderWithToStringExcludeOldBuilder();
  }
  public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
    return (("BuilderWithToStringExcludeOld(a=" + this.a) + ")");
  }
}
