//CONF: lombok.builder.toBuilderMethodName = mutate
import lombok.Builder;

@Builder(toBuilder = true)
class BuilderToBuilderMethodNameConfig {
	private String name;
	private int age;
}
