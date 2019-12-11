import lombok.Builder;
import lombok.experimental.Tolerate;
		
@Builder(setterPrefix = "with")
public class BuilderWithTolerateWithSetterPrefix {
	private final int value;
	
	public static void main(String[] args) {
		BuilderWithTolerateWithSetterPrefix.builder().withValue("42").build();
	}
	
	public static class BuilderWithTolerateWithSetterPrefixBuilder {
		@Tolerate
		public BuilderWithTolerateWithSetterPrefixBuilder withValue(String s) {
			return this.withValue(Integer.parseInt(s));
		}
	}
}
