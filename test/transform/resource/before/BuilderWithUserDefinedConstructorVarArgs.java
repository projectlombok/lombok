@lombok.Builder
class BuilderWithUserDefinedConstructorVarArgs {
	private int plower;
	private int pUpper;
	private int _foo;
	private int __bar;
	
	
	BuilderWithUserDefinedConstructorVarArgs(int...args) {}

}
