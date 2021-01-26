class BuilderWithUserDefinedConstructorVarArgs {
	private int plower;
	private int pUpper;
	private int _foo;
	private int __bar;

	BuilderWithUserDefinedConstructorVarArgs(int... args) {
	}


	@java.lang.SuppressWarnings("all")
	public static class BuilderWithUserDefinedConstructorVarArgsBuilder {
		@java.lang.SuppressWarnings("all")
		private int plower;
		@java.lang.SuppressWarnings("all")
		private int pUpper;
		@java.lang.SuppressWarnings("all")
		private int _foo;
		@java.lang.SuppressWarnings("all")
		private int __bar;

		@java.lang.SuppressWarnings("all")
		BuilderWithUserDefinedConstructorVarArgsBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderWithUserDefinedConstructorVarArgs.BuilderWithUserDefinedConstructorVarArgsBuilder plower(final int plower) {
			this.plower = plower;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderWithUserDefinedConstructorVarArgs.BuilderWithUserDefinedConstructorVarArgsBuilder pUpper(final int pUpper) {
			this.pUpper = pUpper;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderWithUserDefinedConstructorVarArgs.BuilderWithUserDefinedConstructorVarArgsBuilder _foo(final int _foo) {
			this._foo = _foo;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderWithUserDefinedConstructorVarArgs.BuilderWithUserDefinedConstructorVarArgsBuilder __bar(final int __bar) {
			this.__bar = __bar;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public BuilderWithUserDefinedConstructorVarArgs build() {
			return new BuilderWithUserDefinedConstructorVarArgs(this.plower, this.pUpper, this._foo, this.__bar);
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderWithUserDefinedConstructorVarArgs.BuilderWithUserDefinedConstructorVarArgsBuilder(plower=" + this.plower + ", pUpper=" + this.pUpper + ", _foo=" + this._foo + ", __bar=" + this.__bar + ")";
		}
	}

	@java.lang.SuppressWarnings("all")
	public static BuilderWithUserDefinedConstructorVarArgs.BuilderWithUserDefinedConstructorVarArgsBuilder builder() {
		return new BuilderWithUserDefinedConstructorVarArgs.BuilderWithUserDefinedConstructorVarArgsBuilder();
	}
}