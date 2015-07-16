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
	@lombok.Singular private List<T> bars;
	
	@Builder(toBuilder = true)
	public ConstructorWithToBuilder(String mOne, @Builder.ObtainVia(field = "foo") T bar) {
	}
}

@lombok.experimental.Accessors(prefix = "m")
class StaticWithToBuilder<T> {
	private String mOne, mTwo;
	private T foo;
	@lombok.Singular private List<T> bars;
	
	@Builder(toBuilder = true)
	public static <Z> StaticWithToBuilder<Z> test(String mOne, @Builder.ObtainVia(field = "foo") Z bar) {
		return new StaticWithToBuilder<Z>();
	}
}
