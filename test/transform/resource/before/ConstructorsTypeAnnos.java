//CONF: lombok.copyableAnnotations += TA
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@interface TA {
}
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@interface TB {
}
@lombok.AllArgsConstructor
class ConstructorsTypeAnnos {
	@TA @TB List<String> foo;
}
