import lombok.experimental.Tolerate;
public class BuilderWithTolerate {
	private final int value;
	public static void main(String[] args) {
		BuilderWithTolerate.builder().value("42").build();
	}
	public static class BuilderWithTolerateBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private int value;
		@Tolerate
		public BuilderWithTolerateBuilder value(String s) {
			return this.value(Integer.parseInt(s));
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderWithTolerateBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithTolerate.BuilderWithTolerateBuilder value(final int value) {
			this.value = value;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderWithTolerate build() {
			return new BuilderWithTolerate(this.value);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderWithTolerate.BuilderWithTolerateBuilder(value=" + this.value + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderWithTolerate(final int value) {
		this.value = value;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderWithTolerate.BuilderWithTolerateBuilder builder() {
		return new BuilderWithTolerate.BuilderWithTolerateBuilder();
	}
}
