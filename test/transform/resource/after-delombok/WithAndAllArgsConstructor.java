class WithAndAllArgsConstructor<T, J extends T, L extends java.lang.Number> {
	J test;
	java.util.List<L> test2;
	final int x = 10;
	int y = 20;
	final int z;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public WithAndAllArgsConstructor(final J test, final java.util.List<L> test2, final int y, final int z) {
		this.test = test;
		this.test2 = test2;
		this.y = y;
		this.z = z;
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public WithAndAllArgsConstructor<T, J, L> withTest(final J test) {
		return this.test == test ? this : new WithAndAllArgsConstructor<T, J, L>(test, this.test2, this.y, this.z);
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public WithAndAllArgsConstructor<T, J, L> withTest2(final java.util.List<L> test2) {
		return this.test2 == test2 ? this : new WithAndAllArgsConstructor<T, J, L>(this.test, test2, this.y, this.z);
	}
}
