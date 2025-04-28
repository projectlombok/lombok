import lombok.Builder;
import lombok.ToString;
import lombok.ToString.Exclude;
public @Builder class BuilderWithToStringExclude {
  public static @java.lang.SuppressWarnings("all") @lombok.Generated class BuilderWithToStringExcludeBuilder {
    private @java.lang.SuppressWarnings("all") @lombok.Generated String a;
    private @java.lang.SuppressWarnings("all") @lombok.Generated String secret;
    @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExcludeBuilder() {
      super();
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExclude.BuilderWithToStringExcludeBuilder a(final String a) {
      this.a = a;
      return this;
    }
    /**
     * @return {@code this}.
     */
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExclude.BuilderWithToStringExcludeBuilder secret(final String secret) {
      this.secret = secret;
      return this;
    }
    public @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExclude build() {
      return new BuilderWithToStringExclude(this.a, this.secret);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String toString() {
      return (("BuilderWithToStringExclude.BuilderWithToStringExcludeBuilder(a=" + this.a) + ")");
    }
  }
  private String a;
  private @ToString.Exclude String secret;
  @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExclude(final String a, final String secret) {
    super();
    this.a = a;
    this.secret = secret;
  }
  public static @java.lang.SuppressWarnings("all") @lombok.Generated BuilderWithToStringExclude.BuilderWithToStringExcludeBuilder builder() {
    return new BuilderWithToStringExclude.BuilderWithToStringExcludeBuilder();
  }
}
