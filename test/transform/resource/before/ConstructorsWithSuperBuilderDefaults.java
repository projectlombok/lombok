import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Builder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
class ConstructorsWithSuperBuilderDefaults {
	@Builder.Default int x = 5;
	int y;
}
