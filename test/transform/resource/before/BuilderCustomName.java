//CONF: lombok.builder.className = SimpleTestBuilder
import java.util.List;

@lombok.experimental.SuperBuilder
class BuilderCustomName<T> {
	private final int field;
}
