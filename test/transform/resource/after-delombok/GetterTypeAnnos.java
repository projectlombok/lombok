import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@interface TA {
}
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@interface TB {
}
class GetterTypeAnnos {
	@TA
	@TB
	List<String> foo;
	@TA
	@java.lang.SuppressWarnings("all")
	public List<String> getFoo() {
		return this.foo;
	}
}
