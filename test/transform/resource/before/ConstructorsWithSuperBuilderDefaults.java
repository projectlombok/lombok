import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SuperBuilder;
import lombok.Builder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
class ConstructorsWithSuperBuilderDefaults {
	@Builder.Default int x = 5;
	int y;
}
