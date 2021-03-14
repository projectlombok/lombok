//version 8: springframework dep is too new to run on j6
class WithPlain {
	int i;
	final int foo;
	WithPlain(int i, int foo) {
		this.i = i;
		this.foo = foo;
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@org.springframework.lang.NonNull
	@java.lang.SuppressWarnings("all")
	public WithPlain withI(final int i) {
		return this.i == i ? this : new WithPlain(i, this.foo);
	}
	/**
	 * @return a clone of this object, except with this updated property (returns {@code this} if an identical value is passed).
	 */
	@org.springframework.lang.NonNull
	@java.lang.SuppressWarnings("all")
	public WithPlain withFoo(final int foo) {
		return this.foo == foo ? this : new WithPlain(this.i, foo);
	}
}
