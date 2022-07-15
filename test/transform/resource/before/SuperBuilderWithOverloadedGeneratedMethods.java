public class SuperBuilderWithOverloadedGeneratedMethods {
	@lombok.experimental.SuperBuilder
	public static class Parent {
		int self;
	}
	
	@lombok.experimental.SuperBuilder
	public static class Child extends Parent {
		double build;
	}
	
	public static void test() {
		Child x = Child.builder().build(0.0).self(5).build();
	}
}
