//CONF: lombok.builder.className = SimpleTestBuilder
import java.util.List;

@lombok.SuperBuilder
class SuperBuilderCustomName<T> {
	private final int field;
}
