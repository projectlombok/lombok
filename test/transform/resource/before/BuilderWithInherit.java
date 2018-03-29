import lombok.Builder;
import lombok.Singular;
import java.util.List;

public class BuilderWithInherit {
	@Builder(extensible = true)
	public static class Parent {
		int field1;
		@Singular List<String> items;
	}
	
	@Builder(inherit = true)
	public static class Child extends Parent {
		double field3;
	}
	
	public static void test() {
		Child x = Child.builder().field3(0.0).field1(5).item("").build();
	}
}
