class ConstructorsConfiguration {
	int x;
	@java.lang.SuppressWarnings("all")
	public ConstructorsConfiguration(final int x) {
		this.x = x;
	}
}
class ConstructorsConfigurationExplicit {
	int x;
	@java.beans.ConstructorProperties({"x"})
	@java.lang.SuppressWarnings("all")
	public ConstructorsConfigurationExplicit(final int x) {
		this.x = x;
	}
}
