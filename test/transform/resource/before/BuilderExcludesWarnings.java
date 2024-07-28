import lombok.Builder;

public class BuilderExcludesWarnings {
    long x = System.currentTimeMillis();
    final int y = 5;
    @Builder.Exclude int z;
}