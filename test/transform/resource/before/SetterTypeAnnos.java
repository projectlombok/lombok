//CONF: lombok.copyableAnnotations += TA
import lombok.Setter;
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
	@Setter
	@TA @TB List<String> foo;
}
