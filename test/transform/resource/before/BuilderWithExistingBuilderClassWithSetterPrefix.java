import lombok.Builder;

class BuilderWithExistingBuilderClass<T, K extends Number> {
	@Builder(setterPrefix = "with")
	public static <Z extends Number> BuilderWithExistingBuilderClass<String, Z> staticMethod(Z arg1, boolean arg2, String arg3) {
		return null;
	}
	
	public static class BuilderWithExistingBuilderClassBuilder<Z extends Number> {
		private Z arg1;
		
		public void withArg2(boolean arg) {
		}
	}
}
