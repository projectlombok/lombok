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
      final ValOutersWithGenerics<java.lang.String>.Inner elem = list.get(0);
    }
  }
  public ValOutersWithGenerics() {
    super();
  }
  public void testOutersWithGenerics() {
    List<Inner> list = new ArrayList<Inner>();
    final ValOutersWithGenerics<Z>.Inner elem = list.get(0);
  }
}
