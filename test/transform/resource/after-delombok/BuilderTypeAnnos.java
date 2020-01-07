import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@interface TA {
}
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@interface TB {
}
class BuilderTypeAnnos {
	@TA
	@TB
	private List<String> foo;
	@java.lang.SuppressWarnings("all")
	BuilderTypeAnnos(@TA final List<String> foo) {
		this.foo = foo;
	}
	@java.lang.SuppressWarnings("all")
	public static class BuilderTypeAnnosBuilder {
		@java.lang.SuppressWarnings("all")
		private List<String> foo;
		@java.lang.SuppressWarnings("all")
		BuilderTypeAnnosBuilder() {
		}
		@java.lang.SuppressWarnings("all")
		public BuilderTypeAnnos.BuilderTypeAnnosBuilder foo(@TA final List<String> foo) {
			this.foo = foo;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		public BuilderTypeAnnos build() {
			return new BuilderTypeAnnos(this.foo);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderTypeAnnos.BuilderTypeAnnosBuilder(foo=" + this.foo + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	public static BuilderTypeAnnos.BuilderTypeAnnosBuilder builder() {
		return new BuilderTypeAnnos.BuilderTypeAnnosBuilder();
	}
}

