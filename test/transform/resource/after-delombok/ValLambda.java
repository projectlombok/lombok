// version 8:
class ValLambda {
	public void easyLambda() {
		final java.lang.Runnable foo = (Runnable) () -> {
		};
	}
	public void easyIntersectionLambda() {
		final java.lang.Object foo = (Runnable & java.io.Serializable) () -> {
		};
		final java.lang.Object bar = (java.io.Serializable & Runnable) () -> {
		};
	}
	public void easyLubLambda() {
		final java.lang.Runnable foo = (System.currentTimeMillis() > 0) ? (Runnable) () -> {
		} : (Runnable) System.out::println;
	}
//	public void castLubLambda() {
//		Runnable foo = (Runnable) ((System.currentTimeMillis() > 0) ? () -> {} : System.out::println);
//		lombok.val foo = (Runnable) ((System.currentTimeMillis() > 0) ? () -> {} : System.out::println);
//	}
}
