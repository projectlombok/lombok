// version 8:
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

public class TypeAnnotations {
	@Target({ElementType.TYPE_USE, ElementType.LOCAL_VARIABLE})
	@interface Foo {}
	
	@Target(ElementType.TYPE_USE)
	@interface Bar {}
	
	@Target(ElementType.TYPE_USE)
	@interface Baz {}
	
	@Target(ElementType.TYPE_USE)
	@interface Bat {}
	
	public List<@Foo String> test(@Foo String param) {
		@Bar String local = "bar";
		@Foo java.io.@Foo File[] array = {};
		return new java.util.@Foo ArrayList<java.lang.@Foo String>();
	}
	
	public <@Foo T extends java.lang.@Foo Number> T test2(@Bar String... varargs) {
		return null;
	}
	
	public void test3(String[][] arg, String[]... arg2) {
		@Baz String @Bar [] @Foo [] x;
	}
	
	public void test4(@Foo String @Bar [] @Baz [] @Bat ... y) {
	}
}
