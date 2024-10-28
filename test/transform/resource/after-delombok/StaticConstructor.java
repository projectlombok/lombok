//version 8:
public class StaticConstructor {
	String name;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private StaticConstructor(final String name) {
		this.name = name;
	}
	@org.checkerframework.checker.nullness.qual.NonNull
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static StaticConstructor of(final String name) {
		return new StaticConstructor(name);
	}
}
