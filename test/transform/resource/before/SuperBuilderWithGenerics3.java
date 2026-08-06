import java.util.List;

public class SuperBuilderWithGenerics3 {
	@lombok.SuperBuilder
	public static class Parent<A> {
		private final String str;
	}
	
	@lombok.SuperBuilder
	public static class Child extends Parent<Child.SomeInnerStaticClass> {
		public static class SomeInnerStaticClass { }
		double field3;
	}
}
