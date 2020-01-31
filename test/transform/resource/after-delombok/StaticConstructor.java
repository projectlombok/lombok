public class StaticConstructor {
	String name;
	@java.lang.SuppressWarnings("all")
	private StaticConstructor(final String name) {
		this.name = name;
	}
	@org.checkerframework.checker.nullness.qual.NonNull
	@java.lang.SuppressWarnings("all")
	public static StaticConstructor of(final String name) {
		return new StaticConstructor(name);
	}
}