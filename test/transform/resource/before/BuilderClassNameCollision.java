//skip compare content
import lombok.Builder;

@Builder(builderClassName = "Builder")
class BuilderClassNameCollision<T> {
	T value;
}
