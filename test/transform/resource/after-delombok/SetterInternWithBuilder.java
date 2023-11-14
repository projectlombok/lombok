public class SetterInternWithBuilder {
	private String str1;

	@java.lang.SuppressWarnings("all")
	SetterInternWithBuilder(final String str1) {
		this.str1 = str1;
	}


	@java.lang.SuppressWarnings("all")
	public static class SetterInternWithBuilderBuilder {
		@java.lang.SuppressWarnings("all")
		private String str1;

		@java.lang.SuppressWarnings("all")
		SetterInternWithBuilderBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		public SetterInternWithBuilder.SetterInternWithBuilderBuilder str1(final String str1) {
			this.str1 = str1;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public SetterInternWithBuilder build() {
			return new SetterInternWithBuilder(this.str1);
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "SetterInternWithBuilder.SetterInternWithBuilderBuilder(str1=" + this.str1 + ")";
		}
	}

	@java.lang.SuppressWarnings("all")
	public static SetterInternWithBuilder.SetterInternWithBuilderBuilder builder() {
		return new SetterInternWithBuilder.SetterInternWithBuilderBuilder();
	}

	@java.lang.SuppressWarnings("all")
	public void setStr1(final String str1) {
		this.str1 = str1 == null ? null : str1.intern();
	}
}