import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;

@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@interface TA {
}

class WitherTypeAnnos {
	@TA
	final List<@TA String> foo;

	WitherTypeAnnos(@TA List<@TA String> foo) {
		this.foo = foo;
	}

	@java.lang.SuppressWarnings("all")
	public WitherTypeAnnos withFoo(@TA final List<@TA String> foo) {
		return this.foo == foo ? this : new WitherTypeAnnos(foo);
	}
}
