import java.util.*;
import lombok.val;
public class ValWeirdTypes<Z> {
  private List<Z> fieldList;
  public ValWeirdTypes() {
    super();
  }
  public void testGenerics() {
    List<String> list = new ArrayList<String>();
    list.add("Hello, World!");
    final @val java.lang.String shouldBeString = list.get(0);
    final @val java.util.List<java.lang.String> shouldBeListOfString = list;
    final @val java.util.List<java.lang.String> shouldBeListOfStringToo = Arrays.asList("hello", "world");
    final @val java.lang.String shouldBeString2 = shouldBeListOfString.get(0);
  }
  public void testGenericsInference() {
    final @val java.util.List<java.lang.Object> huh = Collections.emptyList();
    final @val java.util.List<java.lang.Number> huh2 = Collections.<Number>emptyList();
  }
  public void testPrimitives() {
    final @val int x = 10;
    final @val long y = (5 + 3L);
  }
  public void testAnonymousInnerClass() {
    final @val java.lang.Runnable y = new Runnable() {
      x() {
        super();
      }
      public void run() {
      }
    };
  }
  public <T extends Number>void testTypeParams(List<T> param) {
    final @val T t = param.get(0);
    final @val Z z = fieldList.get(0);
    final @val java.util.List<T> k = param;
    final @val java.util.List<Z> y = fieldList;
  }
  public void testBounds(List<? extends Number> lower, List<? super Number> upper) {
    final @val java.lang.Number a = lower.get(0);
    final @val java.lang.Object b = upper.get(0);
    final @val java.util.List<? extends java.lang.Number> c = lower;
    final @val java.util.List<? super java.lang.Number> d = upper;
    List<?> unbound = lower;
    final @val java.util.List<?> e = unbound;
  }
  public void testCompound() {
    final @val java.util.ArrayList<java.lang.String> a = new ArrayList<String>();
    final @val java.util.Vector<java.lang.String> b = new Vector<String>();
    final @val boolean c = (1 < System.currentTimeMillis());
    final @val java.util.AbstractList<java.lang.String> d = (c ? a : b);
    java.util.RandomAccess confirm = (c ? a : b);
  }
  public void nullType() {
    final @val java.lang.Object nully = null;
  }
  public void testArrays() {
    final @val int[] intArray = new int[]{1, 2, 3};
    final @val java.lang.Object[][] multiDimArray = new Object[][]{{}};
    final @val int[] copy = intArray;
    final @val java.lang.Object[] single = multiDimArray[0];
    final @val int singleInt = copy[0];
  }
}