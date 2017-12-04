@lombok.experimental.Builder(fluent = false,chain = false) class BuilderChainAndFluent {
  public static @java.lang.SuppressWarnings("all") class BuilderChainAndFluentBuilder {
    private @java.lang.SuppressWarnings("all") int yes;
    @java.lang.SuppressWarnings("all") BuilderChainAndFluentBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") void setYes(final int yes) {
      this.yes = yes;
    }
    public @java.lang.SuppressWarnings("all") BuilderChainAndFluent build() {
      return new BuilderChainAndFluent(yes);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") java.lang.String toString() {
      return (("BuilderChainAndFluent.BuilderChainAndFluentBuilder(yes=" + this.yes) + ")");
    }
  }
  private final int yes;
  @java.lang.SuppressWarnings("all") BuilderChainAndFluent(final int yes) {
    super();
    this.yes = yes;
  }
  public static @java.lang.SuppressWarnings("all") BuilderChainAndFluentBuilder builder() {
    return new BuilderChainAndFluentBuilder();
  }
}
