import java.util.*;
import lombok.val;
public class ValOutersWithGenerics<Z> {
  class Inner {
    Inner() {
      super();
    }
  }
  static class SubClass extends ValOutersWithGenerics<String> {
    SubClass() {
      super();
    }
    public void testSubClassOfOutersWithGenerics() {
      List<Inner> list = new ArrayList<Inner>();
      final @val ValOutersWithGenerics<java.lang.String>.Inner elem = list.get(0);
    }
  }
  public ValOutersWithGenerics() {
    super();
  }
  public void testOutersWithGenerics() {
    final @val java.lang.String foo = "";
    List<Inner> list = new ArrayList<Inner>();
    final @val ValOutersWithGenerics<Z>.Inner elem = list.get(0);
  }
  public void testLocalClasses() {
    class Local<A> {
      Local() {
        super();
      }
    }
    final @val Local<java.lang.String> q = new Local<String>();
  }
}
