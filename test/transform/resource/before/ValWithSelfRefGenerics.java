// version :9
import lombok.val;
public class ValWithSelfRefGenerics {
	public void run(Thing<? extends Comparable<?>> thing, Thing<?> thing2, java.util.List<? extends Number> z) {
		val y = z;
		val x = thing;
		val w = thing2;
		val v = thing2.get();
	}
}
class Thing<T extends Comparable<? super T>> {
	public T get() {
		return null;
	}
}
