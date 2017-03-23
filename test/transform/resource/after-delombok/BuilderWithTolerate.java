import lombok.experimental.Tolerate;
public class BuilderWithTolerate {
	private final int value;
	public static void main(String[] args) {
		BuilderWithTolerate.builder().value("42").build();
	}
	public static class BuilderWithTolerateBuilder {
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		private int value;
		@Tolerate
		public BuilderWithTolerateBuilder value(String s) {
			return this.value(Integer.parseInt(s));
		}
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		BuilderWithTolerateBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		public BuilderWithTolerateBuilder value(final int value) {
			this.value = value;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		public BuilderWithTolerate build() {
			return new BuilderWithTolerate(value);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		public java.lang.String toString() {
			return "BuilderWithTolerate.BuilderWithTolerateBuilder(value=" + this.value + ")";
		}
	}
	@java.beans.ConstructorProperties({"value"})
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	BuilderWithTolerate(final int value) {
		this.value = value;
	}
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	public static BuilderWithTolerateBuilder builder() {
		return new BuilderWithTolerateBuilder();
	}
}
