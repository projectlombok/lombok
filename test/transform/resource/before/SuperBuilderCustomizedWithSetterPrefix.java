import java.util.List;

public class SuperBuilderCustomizedWithSetterPrefix {
	@lombok.experimental.SuperBuilder(setterPrefix = "set")
	public static class Parent {
		public static abstract class ParentBuilder<C extends Parent, B extends ParentBuilder<C, B>> {
			public B setField1(int field1) {
				this.field1 = field1 + 1;
				return self();
			}
		}
		int field1;
	}
	
	public static void test() {
		Parent x = Parent.builder().setField1(5).build();
	}
}
