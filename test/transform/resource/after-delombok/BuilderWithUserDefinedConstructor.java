class BuilderWithUserDefinedConstructor {
	private int plower;
	private int pUpper;
	private int _foo;
	private int __bar;

	BuilderWithUserDefinedConstructor(int plower, int pUpper) {
		this.plower = plower;
		this.pUpper = pUpper;
	}

	@java.lang.SuppressWarnings("all")
	BuilderWithUserDefinedConstructor(final int plower, final int pUpper, final int _foo, final int __bar) {
		this.plower = plower;
		this.pUpper = pUpper;
		this._foo = _foo;
		this.__bar = __bar;
	}


	@java.lang.SuppressWarnings("all")
	public static class BuilderWithUserDefinedConstructorBuilder {
		@java.lang.SuppressWarnings("all")
		private int plower;
		@java.lang.SuppressWarnings("all")
		private int pUpper;
		@java.lang.SuppressWarnings("all")
		private int _foo;
		@java.lang.SuppressWarnings("all")
		private int __bar;

		@java.lang.SuppressWarnings("all")
		BuilderWithUserDefinedConstructorBuilder() {
		}

		@java.lang.SuppressWarnings("all")
		public BuilderWithUserDefinedConstructor.BuilderWithUserDefinedConstructorBuilder plower(final int plower) {
			this.plower = plower;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public BuilderWithUserDefinedConstructor.BuilderWithUserDefinedConstructorBuilder pUpper(final int pUpper) {
			this.pUpper = pUpper;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public BuilderWithUserDefinedConstructor.BuilderWithUserDefinedConstructorBuilder _foo(final int _foo) {
			this._foo = _foo;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public BuilderWithUserDefinedConstructor.BuilderWithUserDefinedConstructorBuilder __bar(final int __bar) {
			this.__bar = __bar;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public BuilderWithUserDefinedConstructor build() {
			return new BuilderWithUserDefinedConstructor(this.plower, this.pUpper, this._foo, this.__bar);
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderWithUserDefinedConstructor.BuilderWithUserDefinedConstructorBuilder(plower=" + this.plower + ", pUpper=" + this.pUpper + ", _foo=" + this._foo + ", __bar=" + this.__bar + ")";
		}
	}

	@java.lang.SuppressWarnings("all")
	public static BuilderWithUserDefinedConstructor.BuilderWithUserDefinedConstructorBuilder builder() {
		return new BuilderWithUserDefinedConstructor.BuilderWithUserDefinedConstructorBuilder();
	}
}
