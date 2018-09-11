//CONF: lombok.copyAnnotations += TA

import lombok.Setter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;

@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@interface TA {
}

class SetterTypeAnnos {
	@Setter
	@TA List<@TA String> foo;
}
