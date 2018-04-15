import lombok.experimental.SuperBuilder;
import lombok.Singular;
import java.util.List;

public class SuperBuilderWithGenerics {
	@SuperBuilder
	public static class Parent<A> {
		A field1;
		@Singular List<String> items;
	}
	
	@SuperBuilder
	public static class Child<A> extends Parent<A> {
		double field3;
	}
	
	public static void test() {
		Child<Integer> x = Child.<Integer>builder().field3(0.0).field1(5).item("").build();
	}
}
