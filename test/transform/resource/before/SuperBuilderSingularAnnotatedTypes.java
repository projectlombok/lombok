//version 8:
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.Map;
import lombok.NonNull;
import lombok.Singular;
@Target(ElementType.TYPE_USE)
@interface MyAnnotation {}
@lombok.experimental.SuperBuilder
class SuperBuilderSingularAnnotatedTypes {
	@Singular private Set<@MyAnnotation @NonNull String> foos;
	@Singular private Map<@MyAnnotation @NonNull String, @MyAnnotation @NonNull Integer> bars;
}
