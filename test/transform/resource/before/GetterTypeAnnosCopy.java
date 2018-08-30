//CONF: lombok.copyAnnotations += TA

import lombok.Getter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;

@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@interface TA {
}

class GetterTypeAnnos {
	@Getter
       	@TA List<@TA String> foo;
}
