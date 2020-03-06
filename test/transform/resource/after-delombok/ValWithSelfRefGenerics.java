public class ValWithSelfRefGenerics {
	public void run(Thing<? extends Comparable<?>> thing, Thing<?> thing2, java.util.List<? extends Number> z) {
		final java.util.List<? extends java.lang.Number> y = z;
		final Thing<? extends java.lang.Comparable<?>> x = thing;
		final Thing<?> w = thing2;
		final java.lang.Comparable<?> v = thing2.get();
	}
}

class Thing<T extends Comparable<? super T>> {
	public T get() {
		return null;
	}
}