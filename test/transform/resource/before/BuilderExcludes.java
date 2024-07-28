import lombok.Builder;

@Builder
public class BuilderExcludes {
    long x = System.currentTimeMillis();
    final int y = 5;
    @Builder.Exclude int z;
}