@lombok.Builder
class BuilderWithSuperObjectsDefinedConstructor {
	private String __bar;
	private Long pUpper;
	
	public BuilderWithSuperObjectsDefinedConstructor(CharSequence a, Number b) {
		__bar = (String)a;
		pUpper = (Long)b;
	}
}
