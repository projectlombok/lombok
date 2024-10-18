import lombok.Builder;

@Builder
public class BuilderExcludesWarnings {
    long x;
    int y;
    @Builder.Exclude int z;
}

class NoBuilderButHasExcludes {
    long x;
    int y;
    @Builder.Exclude int z;

    @Builder
    public NoBuilderButHasExcludes(long x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

