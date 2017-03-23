@lombok.experimental.Builder(fluent = false,chain = false) class BuilderChainAndFluent {
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") class BuilderChainAndFluentBuilder {
    private @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") int yes;
    @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderChainAndFluentBuilder() {
      super();
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") void setYes(final int yes) {
      this.yes = yes;
    }
    public @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderChainAndFluent build() {
      return new BuilderChainAndFluent(yes);
    }
    public @java.lang.Override @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") java.lang.String toString() {
      return (("BuilderChainAndFluent.BuilderChainAndFluentBuilder(yes=" + this.yes) + ")");
    }
  }
  private final int yes;
  @java.beans.ConstructorProperties({"yes"}) @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderChainAndFluent(final int yes) {
    super();
    this.yes = yes;
  }
  public static @java.lang.SuppressWarnings("all") @javax.annotation.Generated("lombok") BuilderChainAndFluentBuilder builder() {
    return new BuilderChainAndFluentBuilder();
  }
}
