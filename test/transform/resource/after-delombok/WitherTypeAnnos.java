import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@interface TA {
}
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@interface TB {
}
class WitherTypeAnnos {
	@TA
	@TB
	final List<String> foo;
	WitherTypeAnnos(@TA @TB List<String> foo) {
		this.foo = foo;
	}
	@java.lang.SuppressWarnings("all")
	public WitherTypeAnnos withFoo(@TA final List<String> foo) {
		return this.foo == foo ? this : new WitherTypeAnnos(foo);
	}
}
