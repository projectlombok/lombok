class BuilderWithRequiredArgsConstructor {
	private int plower;
	private Long pUpper;
	private final long _foo;
	private final String __bar;

	@java.lang.SuppressWarnings("all")
	BuilderWithRequiredArgsConstructor(final int plower, final Long pUpper, final long _foo, final String __bar) {
		this.plower = plower;
		this.pUpper = pUpper;
		this._foo = _foo;
		this.__bar = __bar;
	}


	@java.lang.SuppressWarnings("all")
	public static class BuilderWithRequiredArgsConstructorBuilder {
		@java.lang.SuppressWarnings("all")
		private int plower;
		@java.lang.SuppressWarnings("all")
		private Long pUpper;
		@java.lang.SuppressWarnings("all")
		private long _foo;
		@java.lang.SuppressWarnings("all")
		private String __bar;

		@java.lang.SuppressWarnings("all")
		BuilderWithRequiredArgsConstructorBuilder() {
		}

		@java.lang.SuppressWarnings("all")
		public BuilderWithRequiredArgsConstructor.BuilderWithRequiredArgsConstructorBuilder plower(final int plower) {
			this.plower = plower;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public BuilderWithRequiredArgsConstructor.BuilderWithRequiredArgsConstructorBuilder pUpper(final Long pUpper) {
			this.pUpper = pUpper;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public BuilderWithRequiredArgsConstructor.BuilderWithRequiredArgsConstructorBuilder _foo(final long _foo) {
			this._foo = _foo;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public BuilderWithRequiredArgsConstructor.BuilderWithRequiredArgsConstructorBuilder __bar(final String __bar) {
			this.__bar = __bar;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public BuilderWithRequiredArgsConstructor build() {
			return new BuilderWithRequiredArgsConstructor(this.plower, this.pUpper, this._foo, this.__bar);
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderWithRequiredArgsConstructor.BuilderWithRequiredArgsConstructorBuilder(plower=" + this.plower + ", pUpper=" + this.pUpper + ", _foo=" + this._foo + ", __bar=" + this.__bar + ")";
		}
	}

	@java.lang.SuppressWarnings("all")
	public static BuilderWithRequiredArgsConstructor.BuilderWithRequiredArgsConstructorBuilder builder() {
		return new BuilderWithRequiredArgsConstructor.BuilderWithRequiredArgsConstructorBuilder();
	}

	@java.lang.SuppressWarnings("all")
	public BuilderWithRequiredArgsConstructor(final long _foo, final String __bar) {
		this._foo = _foo;
		this.__bar = __bar;
	}
}
