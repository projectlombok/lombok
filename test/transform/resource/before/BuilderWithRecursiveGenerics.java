//issue #1298
import java.util.Set;
import lombok.Builder;
import lombok.Value;

public class BuilderWithRecursiveGenerics {
	interface Inter<T, U extends Inter<T, U>> {}
	
	@Builder @Value public static class Test<Foo, Bar extends Set<Foo>, Quz extends Inter<Bar, Quz>> {
		Foo foo;
		Bar bar;
	}
}
