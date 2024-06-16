@lombok.Builder
public class BuilderNestedJavadoc {
	@lombok.Builder
	public static class NestedClass {
		/**
		 * Example javadoc
		 */
		String name;
	}
}
