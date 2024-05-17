import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@interface TA {
}
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@interface TB {
}
class BuilderTypeAnnosWithSetterPrefix {
	@TA
	@TB
	private List<String> foo;
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	BuilderTypeAnnosWithSetterPrefix(@TA final List<String> foo) {
		this.foo = foo;
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static class BuilderTypeAnnosWithSetterPrefixBuilder {
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		private List<String> foo;
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		BuilderTypeAnnosWithSetterPrefixBuilder() {
		}
		/**
		 * @return {@code this}.
		 */
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderTypeAnnosWithSetterPrefix.BuilderTypeAnnosWithSetterPrefixBuilder withFoo(@TA final List<String> foo) {
			this.foo = foo;
			return this;
		}
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public BuilderTypeAnnosWithSetterPrefix build() {
			return new BuilderTypeAnnosWithSetterPrefix(this.foo);
		}
		@java.lang.Override
		@java.lang.SuppressWarnings("all")
		@lombok.Generated
		public java.lang.String toString() {
			return "BuilderTypeAnnosWithSetterPrefix.BuilderTypeAnnosWithSetterPrefixBuilder(foo=" + this.foo + ")";
		}
	}
	@java.lang.SuppressWarnings("all")
	@lombok.Generated
	public static BuilderTypeAnnosWithSetterPrefix.BuilderTypeAnnosWithSetterPrefixBuilder builder() {
		return new BuilderTypeAnnosWithSetterPrefix.BuilderTypeAnnosWithSetterPrefixBuilder();
	}
}
