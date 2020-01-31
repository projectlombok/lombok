class WithPlain {
	int i;
	final int foo;
	WithPlain(int i, int foo) {
		this.i = i;
		this.foo = foo;
	}
	@org.springframework.lang.NonNull
	@java.lang.SuppressWarnings("all")
	public WithPlain withI(final int i) {
		return this.i == i ? this : new WithPlain(i, this.foo);
	}
	@org.springframework.lang.NonNull
	@java.lang.SuppressWarnings("all")
	public WithPlain withFoo(final int foo) {
		return this.foo == foo ? this : new WithPlain(this.i, foo);
	}
}
