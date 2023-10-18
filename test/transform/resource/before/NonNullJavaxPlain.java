//version 8:
import java.lang.annotation.*;

@lombok.RequiredArgsConstructor
@lombok.Getter
@lombok.Setter
class NonNullJavaxPlain {
	@javax.annotation.Nonnull
	int i;
	@javax.annotation.Nonnull
	String s;
}