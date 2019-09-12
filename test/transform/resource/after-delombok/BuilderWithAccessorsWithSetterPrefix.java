class BuilderWithAccessorsWithSetterPrefix {
	private final int plower;
	private final int pUpper;
	private int _foo;
	private int __bar;
	@java.lang.SuppressWarnings("all")
	BuilderWithAccessorsWithSetterPrefix(final int plower, final int upper, final int foo, final int _bar) {
		this.plower = plower;
		this.pUpper = upper;
		this._foo = foo;
		this.__bar = _bar;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderWithAccessorsWithSetterPrefixBuilder {
		@java.lang.SuppressWarnings("all")
		private int plower;
		@java.lang.SuppressWarnings("all")
		private int upper;
		@java.lang.SuppressWarnings("all")
		private int foo;
		@java.lang.SuppressWarnings("all")
		private int _bar;
		@java.lang.SuppressWarnings("all")
		BuilderWithAccessorsWithSetterPrefixBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithAccessorsWithSetterPrefixBuilder withPlower(final int plower) {
			this.plower = plower;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithAccessorsWithSetterPrefixBuilder withUpper(final int upper) {
			this.upper = upper;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithAccessorsWithSetterPrefixBuilder withFoo(final int foo) {
			this.foo = foo;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithAccessorsWithSetterPrefixBuilder with_Bar(final int _bar) {
			this._bar = _bar;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithAccessorsWithSetterPrefix build() {
			return new BuilderWithAccessorsWithSetterPrefix(plower, upper, foo, _bar);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderWithAccessorsWithSetterPrefix.BuilderWithAccessorsWithSetterPrefixBuilder(plower=" + this.plower + ", upper=" + this.upper + ", foo=" + this.foo + ", _bar=" + this._bar + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderWithAccessorsWithSetterPrefixBuilder builder() {
		return new BuilderWithAccessorsWithSetterPrefixBuilder();
	}
}
