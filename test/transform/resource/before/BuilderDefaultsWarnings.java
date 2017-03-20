import lombok.Builder;

@Builder
public class BuilderDefaultsWarnings {
	long x = System.currentTimeMillis();
	final int y = 5;
	@Builder.Default int z;
}
