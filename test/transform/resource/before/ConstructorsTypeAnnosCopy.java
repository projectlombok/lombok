//CONF: lombok.copyAnnotations += TA

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;

@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@interface TA {
}

@lombok.AllArgsConstructor
class ConstructorsTypeAnnos {
	@TA List<@TA String> foo;
}
