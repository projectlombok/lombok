@lombok.Builder
class BuilderWithUserDefinedConstructor {
	private int plower;
	private int pUpper;
	private int _foo;
	private int __bar;
	
	
	BuilderWithUserDefinedConstructor(int plower, int pUpper) {
		this.plower = plower;
		this.pUpper = pUpper;
	}

}
