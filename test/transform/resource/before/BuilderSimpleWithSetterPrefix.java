import java.util.List;

@lombok.Builder(access = lombok.AccessLevel.PROTECTED, setterPrefix = "with")
class BuilderSimpleWithSetterPrefix<T> {
	private int unprefixed;
}
