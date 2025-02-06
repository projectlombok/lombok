import lombok.Builder;
import lombok.AllArgsConstructor;

@Builder
@AllArgsConstructor
public class BuilderExcludesWithAllArgsConstructor {
    long x;
    int y;
    @Builder.Exclude int z;
}