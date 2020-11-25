import java.util.List;
import lombok.Builder;
@Builder(toBuilder = true) @lombok.experimental.Accessors(prefix = "m")
class BuilderWithToBuilder<T> {
	private String mOne, mTwo;
	@Builder.ObtainVia(method = "rrr", isStatic = true) private T foo;
	@lombok.Singular private List<T> bars;
	public static <K> K rrr(BuilderWithToBuilder<K> x) {
		return x.foo;
	}
}
@lombok.experimental.Accessors(prefix = "m")
class ConstructorWithToBuilder<T> {
	private String mOne, mTwo;
	private T foo;
	@lombok.Singular private com.google.common.collect.ImmutableList<T> bars;
	@Builder(toBuilder = true)
	public ConstructorWithToBuilder(String mOne, @Builder.ObtainVia(field = "foo") T baz, com.google.common.collect.ImmutableList<T> bars) {
	}
}

class StaticMethodWithToBuilder<T> {
	private T foo;
	
	public StaticMethodWithToBuilder(T foo) {
		this.foo = foo;
	}

    @Builder(toBuilder = true)
    public static <T> StaticMethodWithToBuilder<T> of(T foo) {
        return new StaticMethodWithToBuilder<T>(foo);
    }
}