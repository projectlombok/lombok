// version 8:
import java.io.Serializable;

class ValLambda {
	
	static {
		lombok.val foo = (System.currentTimeMillis() > 0) ? (Runnable)()-> {} : System.out::println;
	}
	{
		lombok.val foo = (System.currentTimeMillis() > 0) ? (Runnable)()-> {} : System.out::println;
	}
	
	public void easyLambda() {
		lombok.val foo = (Runnable)()-> {};
	}
	
	public void intersectionLambda() {
		lombok.val foo = (Runnable & Serializable)()-> {};
		lombok.val bar = (java.io.Serializable & Runnable)()-> {};
	}
	
	public void easyLubLambda() {
		lombok.val foo = (System.currentTimeMillis() > 0) ? (Runnable)()-> {} : System.out::println;
		lombok.val foo1 = (System.currentTimeMillis() > 0) ? (Runnable)System.out::println : System.out::println;
		lombok.val foo2 = (System.currentTimeMillis() < 0) ? (java.util.function.Function) r -> "" : r -> System.currentTimeMillis();
		java.util.function.Function foo3 = (System.currentTimeMillis() < 0) ? (java.util.function.Function) r -> "" : r -> System.currentTimeMillis();
		lombok.val foo4 = (System.currentTimeMillis() < 0) ? (java.util.function.Function<String, String>) r -> "" : r -> String.valueOf(System.currentTimeMillis());
	}
}
