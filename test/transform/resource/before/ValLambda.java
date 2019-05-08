// version 8:
class ValLambda {
	public void easyLambda() {
		lombok.val foo = (Runnable)()-> {};
	}
	
	public void easyIntersectionLambda() {
		lombok.val foo = (Runnable & java.io.Serializable)()-> {};
		lombok.val bar = (java.io.Serializable & Runnable)()-> {};
	}
	
	public void easyLubLambda() {
		lombok.val foo = (System.currentTimeMillis() > 0) ? (Runnable)()-> {} : System.out::println;
		lombok.val foo2 = (System.currentTimeMillis() < 0) ? (java.util.function.Function) r -> "" : r -> System.currentTimeMillis();
		java.util.function.Function foo3 = (System.currentTimeMillis() < 0) ? (java.util.function.Function) r -> "" : r -> System.currentTimeMillis();
		lombok.val foo4 = (System.currentTimeMillis() < 0) ? (java.util.function.Function<String, String>) r -> "" : r -> String.valueOf(System.currentTimeMillis());
	}
	
//	public void castLubLambda() {
//		Runnable foo = (Runnable) ((System.currentTimeMillis() > 0) ? () -> {} : System.out::println);
//		lombok.val foo = (Runnable) ((System.currentTimeMillis() > 0) ? () -> {} : System.out::println);
//	}
}
