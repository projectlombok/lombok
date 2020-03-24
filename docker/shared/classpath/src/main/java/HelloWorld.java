@lombok.Data 
public class HelloWorld {
	private final int answer;
	
	public static void main(String... args) {
		System.out.println(new HelloWorld(42).getAnswer());
	}
	
	@FunctionalInterface interface Foo {
		String name();
	}
}
