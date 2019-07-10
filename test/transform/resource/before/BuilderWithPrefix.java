import java.util.List;

@lombok.Builder(access = lombok.AccessLevel.PROTECTED, setterPrefix = "with")
class BuilderWithPrefix<T> {
	private int unprefixed;
}
