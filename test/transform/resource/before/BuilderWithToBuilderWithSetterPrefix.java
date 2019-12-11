import java.util.List;
import lombok.Builder;
@Builder(toBuilder = true, setterPrefix = "with") @lombok.experimental.Accessors(prefix = "m")
class BuilderWithToBuilderWithSetterPrefix<T> {
	private String mOne, mTwo;
	@Builder.ObtainVia(method = "rrr", isStatic = true) private T foo;
	@lombok.Singular private List<T> bars;
	public static <K> K rrr(BuilderWithToBuilderWithSetterPrefix<K> x) {
		return x.foo;
	}
}
@lombok.experimental.Accessors(prefix = "m")
class ConstructorWithToBuilderWithSetterPrefix<T> {
	private String mOne, mTwo;
	private T foo;
	@lombok.Singular private com.google.common.collect.ImmutableList<T> bars;
	@Builder(toBuilder = true, setterPrefix = "with")
	public ConstructorWithToBuilderWithSetterPrefix(String mOne, @Builder.ObtainVia(field = "foo") T baz, com.google.common.collect.ImmutableList<T> bars) {
	}
}
