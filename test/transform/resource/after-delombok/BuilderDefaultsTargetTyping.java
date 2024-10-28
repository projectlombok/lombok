import java.util.Arrays;
public class BuilderDefaultsTargetTyping {
	String foo;
	static String doSth(java.util.List<Integer> i, java.util.List<Character> c) {
		return null;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	private static String $default$foo() {
		return doSth(Arrays.asList(1), Arrays.asList('a'));
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderDefaultsTargetTyping(final String foo) {
		this.foo = foo;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderDefaultsTargetTypingBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private boolean foo$set;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private String foo$value;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderDefaultsTargetTypingBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderDefaultsTargetTyping.BuilderDefaultsTargetTypingBuilder foo(final String foo) {
			this.foo$value = foo;
			foo$set = true;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderDefaultsTargetTyping build() {
			String foo$value = this.foo$value;
			if (!this.foo$set) foo$value = BuilderDefaultsTargetTyping.$default$foo();
			return new BuilderDefaultsTargetTyping(foo$value);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderDefaultsTargetTyping.BuilderDefaultsTargetTypingBuilder(foo$value=" + this.foo$value + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderDefaultsTargetTyping.BuilderDefaultsTargetTypingBuilder builder() {
		return new BuilderDefaultsTargetTyping.BuilderDefaultsTargetTypingBuilder();
	}
}
