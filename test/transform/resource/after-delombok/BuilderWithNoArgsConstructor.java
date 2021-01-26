class BuilderWithNoArgsConstructor {
	private int plower;
	private Long pUpper;
	private long _foo;
	private String __bar;

	@java.lang.SuppressWarnings("all")
	BuilderWithNoArgsConstructor(final int plower, final Long pUpper, final long _foo, final String __bar) {
		this.plower = plower;
		this.pUpper = pUpper;
		this._foo = _foo;
		this.__bar = __bar;
	}


	@java.lang.SuppressWarnings("all")
	public static class BuilderWithNoArgsConstructorBuilder {
		@java.lang.SuppressWarnings("all")
		private int plower;
		@java.lang.SuppressWarnings("all")
		private Long pUpper;
		@java.lang.SuppressWarnings("all")
		private long _foo;
		@java.lang.SuppressWarnings("all")
		private String __bar;

		@java.lang.SuppressWarnings("all")
		BuilderWithNoArgsConstructorBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderWithNoArgsConstructor.BuilderWithNoArgsConstructorBuilder plower(final int plower) {
			this.plower = plower;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderWithNoArgsConstructor.BuilderWithNoArgsConstructorBuilder pUpper(final Long pUpper) {
			this.pUpper = pUpper;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderWithNoArgsConstructor.BuilderWithNoArgsConstructorBuilder _foo(final long _foo) {
			this._foo = _foo;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public BuilderWithNoArgsConstructor.BuilderWithNoArgsConstructorBuilder __bar(final String __bar) {
			this.__bar = __bar;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public BuilderWithNoArgsConstructor build() {
			return new BuilderWithNoArgsConstructor(this.plower, this.pUpper, this._foo, this.__bar);
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderWithNoArgsConstructor.BuilderWithNoArgsConstructorBuilder(plower=" + this.plower + ", pUpper=" + this.pUpper + ", _foo=" + this._foo + ", __bar=" + this.__bar + ")";
		}
	}

	@java.lang.SuppressWarnings("all")
	public static BuilderWithNoArgsConstructor.BuilderWithNoArgsConstructorBuilder builder() {
		return new BuilderWithNoArgsConstructor.BuilderWithNoArgsConstructorBuilder();
	}

	@java.lang.SuppressWarnings("all")
	public BuilderWithNoArgsConstructor() {
	}
}