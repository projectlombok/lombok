//CONF: lombok.copyableAnnotations += TA
import lombok.experimental.Wither;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@interface TA {
}
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@interface TB {
}
class WitherTypeAnnos {
	@Wither final @TA @TB List<String> foo;
	
	WitherTypeAnnos(@TA @TB List<String> foo) {
		this.foo = foo;
	}
}
