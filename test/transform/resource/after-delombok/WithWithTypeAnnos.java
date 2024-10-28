import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@interface TA {
}
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@interface TB {
}
class WithWithTypeAnnos {
	@TA
	@TB
	final List<String> foo;
	WithWithTypeAnnos(@TA @TB List<String> foo) {
		this.foo = foo;
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public WithWithTypeAnnos withFoo(@TA final List<String> foo) {
		return this.foo == foo ? this : new WithWithTypeAnnos(foo);
	}
}
