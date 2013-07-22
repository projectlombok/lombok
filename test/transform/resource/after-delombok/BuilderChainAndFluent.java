class BuilderChainAndFluent {
	private final int yes;
	@java.lang.SuppressWarnings("all")
	BuilderChainAndFluent(final int yes) {
		this.yes = yes;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderChainAndFluentBuilder {
		private int yes;
		@java.lang.SuppressWarnings("all")
		BuilderChainAndFluentBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public void setYes(final int yes) {
			this.yes = yes;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderChainAndFluent build() {
			return new BuilderChainAndFluent(yes);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderChainAndFluent.BuilderChainAndFluentBuilder(yes=" + this.yes + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderChainAndFluentBuilder builder() {
		return new BuilderChainAndFluentBuilder();
	}
}