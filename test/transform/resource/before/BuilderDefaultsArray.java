import lombok.Builder;
import lombok.Value;

@Builder
public class BuilderDefaultsArray {
	@Builder.Default
	int[] x = {1,2};
	
	@Builder.Default
	java.lang.String[][] y = {};
}