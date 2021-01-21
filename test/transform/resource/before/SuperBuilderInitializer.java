import lombok.experimental.SuperBuilder;

class SuperBuilderInitializer {
	@SuperBuilder
	public static class One {
		private String world;
		{
			world = "Hello";
		}
		
		private static final String world2;
		static {
			world2 = "Hello";
		}
	}
}
