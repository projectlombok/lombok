//version 8:
//CONF: lombok.nonNull.useForeignAnnotations = false
import java.lang.annotation.*;

@lombok.RequiredArgsConstructor
@lombok.Getter
@lombok.Setter
class NonNullNoJavaxPlain {
	@javax.annotation.Nonnull
	int i;
	@javax.annotation.Nonnull
	String s;
}