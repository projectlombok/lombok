import java.util.regex.Pattern;

class Tolerate {
	
	private Pattern pattern;
	
	@lombok.experimental.Tolerate
	public void setPattern(String pattern) {
		setPattern(Pattern.compile(pattern));
	}
	
	@java.lang.SuppressWarnings("all")
	
	@javax.annotation.Generated("lombok")
	public void setPattern(final Pattern pattern) {
		this.pattern = pattern;
	}
	
	@java.lang.SuppressWarnings("all")
	
	@javax.annotation.Generated("lombok")
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
	
	@javax.annotation.Generated("lombok")
	public Pattern getPattern() {
		return this.pattern;
	}
	
	@java.lang.SuppressWarnings("all")
	
	@javax.annotation.Generated("lombok")
	public Tolerate2 withPattern(final Pattern pattern) {
		return this.pattern == pattern ? this : new Tolerate2(pattern);
	}
	
	@java.beans.ConstructorProperties({"pattern"})
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public Tolerate2(final Pattern pattern) {
		this.pattern = pattern;
	}
}
final class Tolerate3 {
	private final java.math.RoundingMode mode;
	
	@lombok.experimental.Tolerate
	public int hashCode() {
		return 123456789 * mode.ordinal();
	}
	
	@java.lang.SuppressWarnings("all")
	public java.math.RoundingMode getMode() {
		return this.mode;
	}
	
	@java.beans.ConstructorProperties({"mode"})
	@java.lang.SuppressWarnings("all")
	public Tolerate3(final java.math.RoundingMode mode) {
		this.mode = mode;
	}
	
	@java.lang.Override
	@java.lang.SuppressWarnings("all")
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof Tolerate3)) return false;
		final Tolerate3 other = (Tolerate3)o;
		final java.lang.Object this$mode = this.getMode();
		final java.lang.Object other$mode = other.getMode();
		if (this$mode == null ? other$mode != null : !this$mode.equals(other$mode)) return false;
		return true;
	}
}
