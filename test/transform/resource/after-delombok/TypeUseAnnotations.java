//version 8:
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@interface TA {
	int x();
}
class TypeUseAnnotations {
	List<@TA(x = 5) String> foo;
	List<TypeUseAnnotations.@TA(x = 5) Inner> bar;
	class Inner {
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public List<@TA(x = 5) String> getFoo() {
		return this.foo;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public List<TypeUseAnnotations.@TA(x = 5) Inner> getBar() {
		return this.bar;
	}
}
