import java.util.*;
import lombok.val;
public class ValOutersWithGenerics<Z> {
  class Inner {
    Inner() {
      super();
    }
  }
  class InnerWithGenerics<H> {
    InnerWithGenerics() {
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
  public static void test() {
    final @val ValOutersWithGenerics<java.lang.String> outer = new ValOutersWithGenerics<String>();
    final @val ValOutersWithGenerics<java.lang.String>.Inner inner1 = outer.new Inner();
    final @val ValOutersWithGenerics<java.lang.String>.InnerWithGenerics<java.lang.Integer> inner2 = outer.new InnerWithGenerics<Integer>();
  }
  public static void loop(Map<String, String> map) {
    for (final @val java.util.Map.Entry<java.lang.String, java.lang.String> e : map.entrySet()) 
      {
      }
  }
}
