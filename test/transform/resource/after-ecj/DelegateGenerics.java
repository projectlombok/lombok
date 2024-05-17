public class DelegateGenerics<T> {
  @lombok.experimental.Delegate I1<T> target;
  public DelegateGenerics() {
    super();
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated T a(final T a) {
    return this.target.a(a);
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.String i(final java.lang.String a) {
    return this.target.i(a);
  }
  public @java.lang.SuppressWarnings("all") @lombok.Generated java.lang.Integer t(final java.lang.Integer t) {
    return this.target.t(t);
  }
}
interface I1<T> extends I2<T, Integer, String> {
}
interface I2<A, T, I> extends I3<Integer, I, A> {
}
interface I3<T, I, A> {
  public T t(T t);
  public I i(I a);
  public A a(A a);
}