//CONF: lombok.noArgsConstructor.extraPrivate = true
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.Builder;

@Builder @Value class ConstructorsWithBuilderDefaults {
	@Builder.Default int x = 5;
}
