@lombok.Builder(builderClassName = "Builder")
class BuilderClassNameCollisionQualified<T> {
	private java.util.function.Function<T, String> mapper;
}
