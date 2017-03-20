import lombok.Builder;
import lombok.Value;

@Value @Builder
public class BuilderDefaults {
	@Builder.Default int x = 10;
	String name;
	@Builder.Default long z = System.currentTimeMillis();
}
