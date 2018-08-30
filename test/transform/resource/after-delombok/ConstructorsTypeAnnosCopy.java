import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;

@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@interface TA {
}

class ConstructorsTypeAnnos {
	@TA
	List<@TA String> foo;

	@java.lang.SuppressWarnings("all")
	public ConstructorsTypeAnnos(@TA final List<@TA String> foo) {
		this.foo = foo;
	}
}
