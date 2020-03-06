import lombok.val;
public class ValWithSelfRefGenerics {
  public ValWithSelfRefGenerics() {
    super();
  }
  public void run(Thing<? extends Comparable<?>> thing, Thing<?> thing2, java.util.List<? extends Number> z) {
    final @val java.util.List<? extends java.lang.Number> y = z;
    final @val Thing<? extends java.lang.Comparable<?>> x = thing;
    final @val Thing<?> w = thing2;
    final @val java.lang.Object v = thing2.get();
  }
}
class Thing<T extends Comparable<? super T>> {
  Thing() {
    super();
  }
  public T get() {
    return null;
  }
}