import lombok.experimental.Tolerate;
public class BuilderWithTolerateWithSetterPrefix {
	private final int value;
	public static void main(String[] args) {
		BuilderWithTolerateWithSetterPrefix.builder().withValue("42").build();
	}
	public static class BuilderWithTolerateWithSetterPrefixBuilder {
		@java.lang.SuppressWarnings("all")
		private int value;
		@Tolerate
		public BuilderWithTolerateWithSetterPrefixBuilder withValue(String s) {
			return this.withValue(Integer.parseInt(s));
		}
		@java.lang.SuppressWarnings("all")
		BuilderWithTolerateWithSetterPrefixBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithTolerateWithSetterPrefixBuilder withValue(final int value) {
			this.value = value;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderWithTolerateWithSetterPrefix build() {
			return new BuilderWithTolerateWithSetterPrefix(value);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderWithTolerateWithSetterPrefix.BuilderWithTolerateWithSetterPrefixBuilder(value=" + this.value + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	BuilderWithTolerateWithSetterPrefix(final int value) {
		this.value = value;
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderWithTolerateWithSetterPrefixBuilder builder() {
		return new BuilderWithTolerateWithSetterPrefixBuilder();
	}
}
