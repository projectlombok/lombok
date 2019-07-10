import java.util.Map;

public class SuperBuilderWithGenericsAndToBuilder {
	@lombok.experimental.SuperBuilder(toBuilder = true)
	public static class Parent<A> {
		A field1;
		@lombok.Singular Map<Integer, String> items;
	}
	
	@lombok.experimental.SuperBuilder(toBuilder = true)
	public static class Child<A> extends Parent<A> {
		double field3;
	}
	
	public static void test() {
		Child<Integer> x = Child.<Integer>builder().field3(0.0).field1(5).item(5, "").build().toBuilder().build();
	}
}
