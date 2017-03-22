import lombok.Builder;
import lombok.Singular;

@Builder
public class BuilderDefaultsWarnings {
	long x = System.currentTimeMillis();
	final int y = 5;
	@Builder.Default int z;
	@Builder.Default @Singular java.util.List<String> items;
}

class NoBuilderButHasDefaults {
	@Builder.Default private final long z = 5;
	
	@Builder
	public NoBuilderButHasDefaults() {
	}
}
