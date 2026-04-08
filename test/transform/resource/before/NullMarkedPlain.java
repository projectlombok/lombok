//version 9:
import java.lang.annotation.*;

@lombok.RequiredArgsConstructor
@lombok.Getter
@lombok.Setter
@org.jspecify.annotations.NullMarked
class NullMarkedPlain {
	int i;
	String s;
	@org.jspecify.annotations.Nullable
	Object o;

}