import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;

@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@interface TA {
}

class BuilderTypeAnnos {
	@TA
	private List<@TA String> foo;

	@java.lang.SuppressWarnings("all")
	BuilderTypeAnnos(@TA final List<@TA String> foo) {
		this.foo = foo;
	}


	@java.lang.SuppressWarnings("all")
	public static class BuilderTypeAnnosBuilder {
		@java.lang.SuppressWarnings("all")
		private List<@TA String> foo;

		@java.lang.SuppressWarnings("all")
		BuilderTypeAnnosBuilder() {
		}

		@java.lang.SuppressWarnings("all")
		public BuilderTypeAnnosBuilder foo(@TA final List<@TA String> foo) {
			this.foo = foo;
			return this;
		}

		@java.lang.SuppressWarnings("all")
		public BuilderTypeAnnos build() {
			return new BuilderTypeAnnos(foo);
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		public java.lang.String toString() {
			return "BuilderTypeAnnos.BuilderTypeAnnosBuilder(foo=" + this.foo + ")";
		}
	}

	@java.lang.SuppressWarnings("all")
	public static BuilderTypeAnnosBuilder builder() {
		return new BuilderTypeAnnosBuilder();
	}
}

