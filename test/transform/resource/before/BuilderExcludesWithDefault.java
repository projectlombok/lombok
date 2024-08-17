import lombok.Builder;

@Builder
public class BuilderExcludesWithDefault {
    long x;
    int y;
    @Builder.Exclude @Builder.Default int z = 5;
}