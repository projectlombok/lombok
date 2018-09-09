import java.util.List;

public class SuperBuilderBasicToBuilder {
	@lombok.experimental.SuperBuilder(toBuilder=true)
	public static class Parent {
		int field1;
		@lombok.Singular List<String> items;
	}
	
	@lombok.experimental.SuperBuilder(toBuilder=true)
	public static class Child extends Parent {
		double field3;
	}
	
	public static void test() {
		Child x = Child.builder().field3(0.0).field1(5).item("").build().toBuilder().build();
	}
}
