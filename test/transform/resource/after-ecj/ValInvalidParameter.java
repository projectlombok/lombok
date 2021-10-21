import lombok.val;
public class ValInvalidParameter {
  public ValInvalidParameter() {
    super();
  }
  public void val() {
    final @val java.lang.Object a = a(new NonExistingClass());
    final @val java.lang.Object b = a(a(new NonExistingClass()));
    final @val java.lang.Object c = nonExisitingMethod(b(1));
    final @val java.lang.Object d = nonExistingObject.nonExistingMethod();
    final @val java.lang.Object e = b(1).nonExistingMethod();
    final @val java.lang.Object f = ((1 > 2) ? a(new NonExistingClass()) : a(new NonExistingClass()));
    final @val java.lang.Object g = b2(1);
    final @val java.lang.Object h = b2(a("a"), a(null));
    final @val java.lang.Object i = a(a(null));
  }
  public int a(String param) {
    return 0;
  }
  public int a(Integer param) {
    return 0;
  }
  public Integer b(int i) {
    return i;
  }
  public Integer b2(int i, int j) {
    return i;
  }
}