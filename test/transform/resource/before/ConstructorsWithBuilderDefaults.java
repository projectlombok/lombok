import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.Builder;

@NoArgsConstructor @AllArgsConstructor @Builder @Value class ConstructorsWithBuilderDefaults {
	@Builder.Default int x = 5;
}
