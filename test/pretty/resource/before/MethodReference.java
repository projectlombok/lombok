// version 8:
class MethodReference {
	public MethodReference() {
	}
	
	public <T> MethodReference(T in) {
	}
	
	public void simpleInstanceReference() {
		Runnable r = this::foo;
	}
	
	public void simpleConstructorReference() {
		Runnable r = MethodReference::new;
	}
	
	public void simpleStaticReference() {
		Runnable r = MethodReference::staticFoo;
	}
	
	public void generifiedStaticReference() {
		StringConverter sc = MethodReference::<String>generifiedStaticFoo;
	}
	
	public void generifiedConstructorReference() {
		FooConverter<MethodReference> fc = MethodReference::<String>new;
	}
	
	public void foo() {
	}
	
	private static void staticFoo() {
	}
	
	public static <T> T generifiedStaticFoo(T foo) {
		return foo;
	}
	
	@FunctionalInterface
	public interface StringConverter {
		public String hello(String x);
	}
	
	@FunctionalInterface
	public interface FooConverter<K> {
		public K hello(String x);
	}
}
