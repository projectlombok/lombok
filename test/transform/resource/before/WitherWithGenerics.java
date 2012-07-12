class WitherWithGenerics<T, J extends T, L extends java.lang.Number> {
	@lombok.experimental.Wither J test;
	@lombok.experimental.Wither java.util.List<L> test2;
	int $i;
	
	public WitherWithGenerics(J test, java.util.List<L> test2) {
	}
}