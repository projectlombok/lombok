class WitherWithGenerics<T, J extends T, L extends java.lang.Number> {
	J test;
	java.util.List<L> test2;
	int $i;
	public WitherWithGenerics(J test, java.util.List<L> test2) {
	}
	@java.lang.SuppressWarnings("all")
	public WitherWithGenerics<T, J, L> withTest(final J test) {
		return this.test == test ? this : new WitherWithGenerics<T, J, L>(test, this.test2);
	}
	@java.lang.SuppressWarnings("all")
	public WitherWithGenerics<T, J, L> withTest2(final java.util.List<L> test2) {
		return this.test2 == test2 ? this : new WitherWithGenerics<T, J, L>(this.test, test2);
	}
}
