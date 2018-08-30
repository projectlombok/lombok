//CONF: lombok.copyAnnotations += TA

import lombok.experimental.Wither;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;

@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@interface TA {
}

class WitherTypeAnnos {
	@Wither final @TA List<@TA String> foo;
	
	WitherTypeAnnos(@TA List<@TA String> foo) {
		this.foo = foo;
	}
}
