class BuilderChainAndFluent {
	private final int yes;
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	BuilderChainAndFluent(final int yes) {
		this.yes = yes;
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	public static class BuilderChainAndFluentBuilder {
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		@lombok.Generated
		private int yes;
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		@lombok.Generated
		BuilderChainAndFluentBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		@lombok.Generated
		public void setYes(final int yes) {
			this.yes = yes;
		}
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		@lombok.Generated
		public BuilderChainAndFluent build() {
			return new BuilderChainAndFluent(yes);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderChainAndFluent.BuilderChainAndFluentBuilder(yes=" + this.yes + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	public static BuilderChainAndFluentBuilder builder() {
		return new BuilderChainAndFluentBuilder();
	}
}