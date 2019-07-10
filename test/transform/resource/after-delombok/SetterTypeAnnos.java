import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@interface TA {
}
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@interface TB {
}
class SetterTypeAnnos {
	@TA
	@TB
	List<String> foo;
	@java.lang.SuppressWarnings("all")
	public void setFoo(@TA final List<String> foo) {
		this.foo = foo;
	}
}
