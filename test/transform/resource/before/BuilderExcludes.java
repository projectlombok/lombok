import lombok.Builder;

@Builder
public class BuilderExcludes {
    long x;
    int y;
    @Builder.Exclude int z;
}