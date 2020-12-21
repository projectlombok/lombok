import java.util.regex.Pattern;
class Tolerate {
	private Pattern pattern;
	@lombok.experimental.Tolerate
	public void setPattern(String pattern) {
		setPattern(Pattern.compile(pattern));
	}
	@java.lang.SuppressWarnings("all")
	public void setPattern(final Pattern pattern) {
		this.pattern = pattern;
	}
	@java.lang.SuppressWarnings("all")
	public Pattern getPattern() {
		return this.pattern;
	}
}
class Tolerate2 {
	private final Pattern pattern;
	@lombok.experimental.Tolerate
	public Tolerate2 withPattern(String pattern) {
		return withPattern(Pattern.compile(pattern));
	}
	public Tolerate2 withPattern(String nameGlob, String extensionGlob) {
		return withPattern(nameGlob.replace("*", ".*") + "\\." + extensionGlob.replace("*", ".*"));
	}
	@java.lang.SuppressWarnings("all")
	public Pattern getPattern() {
		return this.pattern;
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	public Tolerate2 withPattern(final Pattern pattern) {
		return this.pattern == pattern ? this : new Tolerate2(pattern);
	}
	@java.lang.SuppressWarnings("all")
	public Tolerate2(final Pattern pattern) {
		this.pattern = pattern;
	}
}
