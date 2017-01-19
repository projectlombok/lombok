@lombok.experimental.Builder(fluent = false,chain = false) class BuilderChainAndFluent {
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated class BuilderChainAndFluentBuilder {
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated int yes;
    @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated BuilderChainAndFluentBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated void setYes(final int yes) {
      this.yes = yes;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated BuilderChainAndFluent build() {
      return new BuilderChainAndFluent(yes);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated java.lang.String toString() {
      return (("BuilderChainAndFluent.BuilderChainAndFluentBuilder(yes=" + this.yes) + ")");
    }
  }
  private final int yes;
  @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated BuilderChainAndFluent(final int yes) {
    super();
    this.yes = yes;
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") @lombok.Generated BuilderChainAndFluentBuilder builder() {
    return new BuilderChainAndFluentBuilder();
  }
}
