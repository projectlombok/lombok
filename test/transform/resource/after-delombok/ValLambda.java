// version 8:
import java.io.Serializable;

class ValLambda {
	static {
		final java.lang.Runnable foo = (System.currentTimeMillis() > 0) ? (Runnable) () -> {
		} : System.out::println;
	}
	
	{
		final java.lang.Runnable foo = (System.currentTimeMillis() > 0) ? (Runnable) () -> {
		} : System.out::println;
	}
	
	public void easyLambda() {
		final java.lang.Runnable foo = (Runnable) () -> {
		};
	}
	
	public void intersectionLambda() {
		final java.io.Serializable foo = (Runnable & Serializable) () -> {
		};
		final java.io.Serializable bar = (java.io.Serializable & Runnable) () -> {
		};
	}
	
	public void easyLubLambda() {
		final java.lang.Runnable foo = (System.currentTimeMillis() > 0) ? (Runnable) () -> {
		} : System.out::println;
		final java.lang.Runnable foo1 = (System.currentTimeMillis() > 0) ? (Runnable) System.out::println : System.out::println;
		final java.util.function.Function foo2 = (System.currentTimeMillis() < 0) ? (java.util.function.Function) r -> "" : r -> System.currentTimeMillis();
		java.util.function.Function foo3 = (System.currentTimeMillis() < 0) ? (java.util.function.Function) r -> "" : r -> System.currentTimeMillis();
		final java.util.function.Function<java.lang.String, java.lang.String> foo4 = (System.currentTimeMillis() < 0) ? (java.util.function.Function<String, String>) r -> "" : r -> String.valueOf(System.currentTimeMillis());
	}
}