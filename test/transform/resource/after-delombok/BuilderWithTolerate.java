
import lombok.experimental.Tolerate;

public class BuilderWithTolerate {
	private final int value;
	
	public static void main(String[] args) {
		BuilderWithTolerate.builder().value("42").build();
	}
	
	
	public static class BuilderWithTolerateBuilder {
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		@lombok.Generated
		private int value;
		
		@Tolerate
		public BuilderWithTolerateBuilder value(String s) {
			return this.value(Integer.parseInt(s));
		}
		
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		@lombok.Generated
		BuilderWithTolerateBuilder() {
		}
		
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		@lombok.Generated
		public BuilderWithTolerateBuilder value(final int value) {
			this.value = value;
			return this;
		}
		
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		@lombok.Generated
		public BuilderWithTolerate build() {
			return new BuilderWithTolerate(value);
		}
		
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@javax.annotation.Generated("lombok")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderWithTolerate.BuilderWithTolerateBuilder(value=" + this.value + ")";
		}
	}
	
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	BuilderWithTolerate(final int value) {
		this.value = value;
	}
	
	@java.lang.SuppressWarnings("all")
	@javax.annotation.Generated("lombok")
	@lombok.Generated
	public static BuilderWithTolerateBuilder builder() {
		return new BuilderWithTolerateBuilder();
	}
}