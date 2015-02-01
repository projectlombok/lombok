import java.util.*;
import lombok.val;
public class ValAnonymousSubclassWithGenerics {
  Object object = new Object() {
    x() {
      super();
    }
    void foo() {
      final @val int j = 1;
    }
  };
  java.util.List<String> names = new java.util.ArrayList<String>() {
    x() {
      super();
    }
    public String get(int i) {
      final @val java.lang.String result = super.get(i);
      return result;
    }
  };
  public ValAnonymousSubclassWithGenerics() {
    super();
  }
  void bar() {
    final @val int k = super.hashCode();
    int x = k;
  }
}