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
		lombok.val foo = (System.currentTimeMillis() > 0) ? (Runnable)()-> {} : (Runnable)System.out::println;
	}
	
//	public void castLubLambda() {
//		Runnable foo = (Runnable) ((System.currentTimeMillis() > 0) ? () -> {} : System.out::println);
//		lombok.val foo = (Runnable) ((System.currentTimeMillis() > 0) ? () -> {} : System.out::println);
//	}
}
