public class DelegateAlreadyImplemented<T> {
  private @lombok.experimental.Delegate A<Integer, T> a;
  public DelegateAlreadyImplemented() {
    super();
  }
  public void a() {
  }
  public void b(java.util.List<String> l) {
  }
  public void c(java.util.List<Integer> l, String[] a, Integer... varargs) {
  }
  public void d(String[][][][] d) {
  }
  public <Y>void e(Y x) {
  }
  public @SuppressWarnings("unchecked") void f(T s, java.util.List<T> l, T[] a, T... varargs) {
  }
  public void g(Number g) {
  }
}
interface A<T, T2> {
  public void a();
  public void b(java.util.List<T> l);
  public @SuppressWarnings("unchecked") void c(java.util.List<T> l, String[] a, T... varargs);
  public void d(String[][][][] d);
  public <X>X e(X x);
  public @SuppressWarnings("unchecked") void f(T2 s, java.util.List<T2> l, T2[] a, T2... varargs);
  public <G extends Number>void g(G g);
}